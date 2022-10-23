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
import com.well.modules.features.call.callFeature.drawing.DrawingFeature
import com.well.modules.features.call.callFeature.webRtc.RtcMsg
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI.Listener.DataChannelState
import com.well.modules.features.call.callHandlers.drawing.DrawingEffectHandler
import com.well.modules.models.CallId
import com.well.modules.models.CallInfo
import com.well.modules.models.Size
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.kotlinUtils.UUID
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer
import com.well.modules.features.call.callFeature.webRtc.RtcMsg.ImageSharingContainer.Msg.UpdateImage as ImgSharingUpdateImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CallEffectHandler(
    private val services: Services,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {
    class Services(
        val callService: CallService?,
        val isConnectedFlow: Flow<Boolean>,
        val onStartOutgoingCall: (CallInfo) -> Unit,
        val onStartedConnecting: () -> Unit,
        val onConnected: () -> Unit,
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
        onRequestImageUpdate = { requestUpdate ->
            services.requestImageUpdate(requestUpdate) { imageContainer ->
                listener(
                    Msg.DrawingMsg(DrawingFeature.Msg.LocalUpdateImage(imageContainer))
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
            private var closed by AtomicRef(false)
            private val handler: CallEffectHandler?
                get() = if (closed) null else this@CallEffectHandler

            override fun close() {
                closed = true
            }

            override fun updateCaptureDimensions(dimensions: Size) {
                listener(Msg.UpdateLocalCaptureDimensions(dimensions))
            }

            override fun updateRemoveVideoContext(viewContext: VideoViewContext?) {
                viewContext?.let {
                    handler?.listener(
                        Msg.UpdateRemoteVideoContext(viewContext)
                    )
                }
            }

            override fun addCandidate(candidate: WebSocketMsg.Call.Candidate) {
                effHandlerScope.launch {
                    handler?.candidates?.emit(candidate)
                }
            }

            override fun sendOffer(webRTCSessionDescriptor: String) {
                handler?.send(WebSocketMsg.Call.Offer(webRTCSessionDescriptor))
            }

            override fun sendAnswer(webRTCSessionDescriptor: String) {
                handler?.send(WebSocketMsg.Call.Answer(webRTCSessionDescriptor))
            }

            private var connectedOnce by AtomicRef(false)

            override fun dataChannelStateChanged(state: DataChannelState) {
                val handler = handler ?: return
                if (state == DataChannelState.Open) {
                    if (!connectedOnce) {
                        connectedOnce = true
                        handler.services.onConnected()
                    }
                    handler.listener(Msg.DataConnectionEstablished)
                } else {
                    handler.listener(Msg.UpdateStatus(State.Status.Connecting))
                }
            }

            override fun receiveData(data: ByteArray) {
                handler?.handleDataChannelMessage(data)
            }
        }
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
            effHandlerScope.launch {
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
            effHandlerScope.launch {
                services.callWebSocketMsgFlow
                    .collect(::listenWebSocketMessage)
            }.asCloseable(),
            webRtcManager,
        ).forEach(::addCloseableChild)
    }

    override suspend fun processEffect(eff: Eff) {
        when (eff) {
            Eff.SystemBack,
            -> Unit
            is Eff.Initiate -> {
                initiateCall(eff)
            }
            is Eff.Accept -> {
                runWebRtcManager {
                    services.onStartedConnecting()
                    sendOffer()
                }
            }
            is Eff.End -> {
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

    private fun initiateCall(eff: Eff.Initiate) {
        val callInfo = object : CallInfo {
            override val id: CallId = CallId.new()
            override val hasVideo: Boolean = eff.hasVideo
            override val user: User = eff.user
        }
        services.onStartOutgoingCall(callInfo)
        send(
            WebSocketMsg.Front.InitiateCall(
                uid = eff.user.id,
                callId = callInfo.id,
                hasVideo = eff.hasVideo
            )
        )
    }

    private fun send(msg: WebSocketMsg.Front) = prepareSend(msg, services.sendFrontWebSocketMsg)

    private fun send(msg: WebSocketMsg.Call) = prepareSend(msg, services.sendCallWebSocketMsg)

    private fun <M : WebSocketMsg> prepareSend(
        msg: M,
        perform: suspend (M) -> Unit,
    ) {
        msg.freeze()
        effHandlerScope.launch {
            perform(msg)
            Napier.i("CallEffectHandler send ws $msg")
        }
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
                        listener(it)
                    }
            }
            is RtcMsg.UpdateDeviceState -> {
                listener(Msg.UpdateRemoteDeviceState(msg.deviceState))
            }
            is RtcMsg.UpdateViewPoint -> {
                listener(
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
                listener(Msg.UpdateRemoteCaptureDimensions(msg.dimensions))
            }
        }
    }

    private fun listenWebSocketMessage(msg: WebSocketMsg.Call) = runWebRtcManager {
        when (msg) {
            is WebSocketMsg.Call.Offer -> {
                listener(Msg.UpdateStatus(State.Status.Connecting))
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
        effHandlerScope.launch {
            block(webRtcManager)
        }
}