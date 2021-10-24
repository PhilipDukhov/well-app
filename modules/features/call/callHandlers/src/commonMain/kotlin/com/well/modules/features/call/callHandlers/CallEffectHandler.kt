package com.well.modules.features.call.callHandlers

import com.well.modules.atomic.AtomicCloseableRef
import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.asCloseable
import com.well.modules.atomic.freeze
import com.well.modules.features.call.callFeature.CallFeature.Eff
import com.well.modules.features.call.callFeature.CallFeature.Msg
import com.well.modules.features.call.callFeature.CallFeature.State
import com.well.modules.features.call.callFeature.VideoViewContext
import com.well.modules.features.call.callHandlers.drawing.DrawingEffectHandler
import com.well.modules.features.call.callFeature.drawing.DrawingFeature
import com.well.modules.features.call.callFeature.webRtc.RtcMsg
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI.Listener.DataChannelState
import com.well.modules.models.Size
import com.well.modules.models.UserId
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.puerh.EffectHandler
import com.well.modules.utils.sharedImage.ImageContainer
import com.well.modules.features.call.callFeature.webRtc.RtcMsg.ImageSharingContainer.Msg.UpdateImage as ImgSharingUpdateImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class CallEffectHandler(
    private val services: Services,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    data class Services(
        val isConnectedFlow: Flow<Boolean>,
        val callWebSocketMsgFlow: Flow<WebSocketMsg.Call>,
        val sendCallWebSocketMsg: suspend (WebSocketMsg.Call) -> Unit,
        val sendFrontWebSocketMsg: suspend (WebSocketMsg.Front) -> Unit,
        val requestImageUpdate: (DrawingFeature.Eff.RequestImageUpdate, (ImageContainer?) -> Unit) -> Unit,
    )

    private val candidates = MutableSharedFlow<WebSocketMsg.Call.Candidate>(replay = Int.MAX_VALUE)
    private val candidatesSendCloseable = AtomicCloseableRef<Closeable>()
    private val webRtcManager: WebRtcManagerI
    private val imageSharingEffectHandler = DrawingEffectHandler(
        webRtcSendListener = {
            send(RtcMsg.ImageSharingContainer(it))
        },
        onRequestImageUpdate = {
            services.requestImageUpdate(it) {
                listener?.invoke(
                    Msg.DrawingMsg(DrawingFeature.Msg.LocalUpdateImage(it))
                )
            }
        }
    )
    private val dataChannelChunkManager = DataChannelChunkManager()

    enum class RawDataMsgId {
        Json,
        Image,
        ;

        fun wrapByteArray(byteArray: ByteArray): ByteArray =
            ordinal.toByteArray() + byteArray

        companion object {
            fun unwrapByteArray(byteArray: ByteArray): Pair<RawDataMsgId, ByteArray> =
                values()
                    .first {
                        it.ordinal == byteArray.readInt()
                    }
                    .let {
                        it to byteArray.copyOfRange(Int.SIZE_BYTES, byteArray.count())
                    }
        }
    }

    init {
        val webRtcManagerListener = object : WebRtcManagerI.Listener, Closeable {
            lateinit var handlerRef: CallEffectHandler

            private var closed by AtomicRef(false)
            private val handler: CallEffectHandler?
                get() = if (closed) null else handlerRef

            override fun close() {
                closed = true
            }

            override fun updateCaptureDimensions(dimensions: Size) {
                listener?.invoke(Msg.UpdateLocalCaptureDimensions(dimensions))
            }

            override fun updateRemoveVideoContext(viewContext: VideoViewContext?) {
                viewContext?.let {
                    handler?.listener?.invoke(
                        Msg.UpdateRemoteVideoContext(viewContext)
                    )
                }
            }

            override fun addCandidate(candidate: WebSocketMsg.Call.Candidate) {
                coroutineScope.launch {
                    handler?.candidates?.emit(candidate)
                }
            }

            override fun sendOffer(webRTCSessionDescriptor: String) {
                handler?.send(WebSocketMsg.Call.Offer(webRTCSessionDescriptor))
            }

            override fun sendAnswer(webRTCSessionDescriptor: String) {
                handler?.send(WebSocketMsg.Call.Answer(webRTCSessionDescriptor))
            }

            override fun dataChannelStateChanged(state: DataChannelState) {
                handler?.listener?.invoke(
                    if (state == DataChannelState.Open) {
                        Msg.DataConnectionEstablished
                    } else {
                        Msg.UpdateStatus(State.Status.Connecting)
                    }
                )
            }

            override fun receiveData(data: ByteArray) {
                handler?.handleDataChannelMessage(data)
            }
        }
        webRtcManagerListener.handlerRef = this
        webRtcManager = webRtcManagerGenerator(
            listOf(
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302",
                "stun:stun2.l.google.com:19302",
                "stun:stun3.l.google.com:19302",
                "stun:stun4.l.google.com:19302",
            ),
            webRtcManagerListener
        ).apply {
            localVideoContext.freeze()
        }
        webRtcManagerListener.freeze()
        listOf(
            webRtcManagerListener,
            coroutineScope.launch {
                services.isConnectedFlow
                    .collect { shouldSend ->
                        candidatesSendCloseable.value =
                            if (shouldSend)
                                runWebRtcManager {
                                    candidates.collect {
                                        services.sendCallWebSocketMsg(it)
                                    }
                                }.asCloseable()
                            else null
                    }
            }.asCloseable(),
            coroutineScope.launch {
                services.callWebSocketMsgFlow
                    .collect(::listenWebSocketMessage)
            }.asCloseable(),
            webRtcManager,
        ).forEach(::addCloseableChild)
    }

    @Suppress("NAME_SHADOWING")
    override fun handleEffect(eff: Eff) {
        when (eff) {
            Eff.SystemBack,
            -> Unit
            is Eff.Initiate ->
                initiateCall(eff.userId)
            is Eff.Accept -> {
                runWebRtcManager {
                    sendOffer()
                }
            }
            Eff.End -> {
                close()
            }
            Eff.ChooseViewPoint -> Unit
            is Eff.SyncLocalDeviceState -> {
                webRtcManager.syncDeviceState(eff.localDeviceState)
            }
            is Eff.NotifyLocalCaptureDimensionsChanged -> {
                send(RtcMsg.UpdateCaptureDimensions(eff.dimensions))
            }
            is Eff.NotifyDeviceStateChanged -> {
                send(RtcMsg.UpdateDeviceState(eff.deviceState))
            }
            is Eff.NotifyUpdateViewPoint -> {
                send(RtcMsg.UpdateViewPoint(eff.viewPoint))
            }
            is Eff.DrawingEff -> {
                imageSharingEffectHandler.handleEffect(eff.eff)
            }
        }
    }

    override fun setListener(listener: suspend (Msg) -> Unit) {
        super.setListener(listener)
        runWebRtcManager {
            listener.invoke(
                Msg.UpdateLocalVideoContext(
                    localVideoContext
                )
            )
        }
    }

    private fun initiateCall(userId: UserId) =
        send(
            WebSocketMsg.Front.InitiateCall(
                userId,
            )
        )

    private fun send(msg: WebSocketMsg.Front) = prepareSend(msg, services.sendFrontWebSocketMsg)

    private fun send(msg: WebSocketMsg.Call) = prepareSend(msg, services.sendCallWebSocketMsg)

    private fun <M : WebSocketMsg> prepareSend(
        msg: M,
        perform: suspend (M) -> Unit
    ) = msg.freeze()
        .let {
            coroutineScope.launch {
                perform(msg)
            }
        }
        .also {
            Napier.i("CallEffectHandler send ws $msg")
        }

    private fun send(msg: RtcMsg) =
        runWebRtcManager {
            msg.freeze()
            dataChannelChunkManager
                .splitByteArrayIntoChunks(
                    when (msg) {
                        is RtcMsg.ImageSharingContainer -> {
                            when (val childMsg = msg.msg) {
                                is ImgSharingUpdateImage -> {
                                    childMsg.imageData?.let { RawDataMsgId.Image.wrapByteArray(it) }
                                }
                                else -> null
                            }
                        }
                        else -> null
                    } ?: RawDataMsgId.Json.wrapByteArray(
                        Json.encodeToString(
                            RtcMsg.serializer(),
                            msg.freeze(),
                        ).encodeToByteArray()
                    )
                )
                .forEach {
                    sendData(it)
                }
        }.also {
            Napier.i("CallEffectHandler send dt $msg")
        }

    private fun handleDataChannelMessage(data: ByteArray) {
        val fullMessageByteArray = dataChannelChunkManager
            .processByteArrayChunk(
                data
            ) ?: return
        val (msgId, unwrappedData) = RawDataMsgId.unwrapByteArray(fullMessageByteArray)
        val msg = when (msgId) {
            RawDataMsgId.Image -> {
                RtcMsg.ImageSharingContainer(ImgSharingUpdateImage(unwrappedData))
            }
            RawDataMsgId.Json -> Json.decodeFromString(
                RtcMsg.serializer(),
                unwrappedData.decodeToString()
            )
        }
        Napier.i("CallEffectHandler got dt $msg")
        when (msg) {
            is RtcMsg.ImageSharingContainer -> {
                imageSharingEffectHandler
                    .handleDataChannelMessage(msg.msg)
                    ?.let {
                        listener?.invoke(it)
                    }
            }
            is RtcMsg.UpdateDeviceState -> {
                listener?.invoke(Msg.UpdateRemoteDeviceState(msg.deviceState))
            }
            is RtcMsg.UpdateViewPoint -> {
                listener?.invoke(
                    Msg.RemoteUpdateViewPoint(
                        when (msg.viewPoint) {
                            State.ViewPoint.Both -> State.ViewPoint.Both
                            State.ViewPoint.Mine -> State.ViewPoint.Partner
                            State.ViewPoint.Partner -> State.ViewPoint.Mine
                        }
                    )
                )
            }
            is RtcMsg.UpdateCaptureDimensions -> {
                listener?.invoke(Msg.UpdateRemoteCaptureDimensions(msg.dimensions))
            }
        }
    }

    private fun listenWebSocketMessage(msg: WebSocketMsg.Call) = runWebRtcManager {
        when (msg) {
            is WebSocketMsg.Call.Offer -> {
                listener?.invoke(Msg.UpdateStatus(State.Status.Connecting))
                acceptOffer(msg.sessionDescriptor)
            }
            is WebSocketMsg.Call.Answer -> {
                acceptAnswer(msg.sessionDescriptor)
            }
            is WebSocketMsg.Call.Candidate -> {
                acceptCandidate(msg)
            }
            else -> Unit
        }
    }

    private fun runWebRtcManager(block: suspend WebRtcManagerI.() -> Unit) =
        coroutineScope.launch {
            block(webRtcManager)
        }
}