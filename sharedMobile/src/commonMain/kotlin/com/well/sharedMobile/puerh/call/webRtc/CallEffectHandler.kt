package com.well.sharedMobile.puerh.call.webRtc

import com.well.serverModels.*
import com.well.sharedMobile.networking.webSocketManager.NetworkManager
import com.well.sharedMobile.puerh.call.*
import com.well.sharedMobile.puerh.call.CallFeature.Eff
import com.well.sharedMobile.puerh.call.CallFeature.Msg
import com.well.sharedMobile.puerh.call.CallFeature.State
import com.well.sharedMobile.puerh.call.webRtc.WebRtcManagerI.Listener.DataChannelState
import com.well.sharedMobile.puerh.call.drawing.DrawingEffectHandler
import com.well.sharedMobile.puerh.call.readInt
import com.well.sharedMobile.puerh.call.toByteArray
import com.well.utils.Closeable
import com.well.utils.EffectHandler
import com.well.utils.asCloseable
import com.well.utils.atomic.AtomicCloseableRef
import com.well.utils.atomic.AtomicRef
import com.well.utils.freeze
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.well.sharedMobile.puerh.call.webRtc.RtcMsg.ImageSharingContainer.Msg.UpdateImage as ImgSharingUpdateImage
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Eff as TopLevelEff
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Msg as TopLevelMsg

class CallEffectHandler(
    private val networkManager: NetworkManager,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<TopLevelEff, TopLevelMsg>(coroutineScope) {
    private val candidatesSendState = networkManager
        .state
        .map { it == NetworkManager.Status.Connected }
        .distinctUntilChanged()
    private val candidates = MutableSharedFlow<WebSocketMessage.Candidate>(replay = Int.MAX_VALUE)
    private val candidatesSendCloseable = AtomicCloseableRef()
    private val webRtcManager: WebRtcManagerI
    private val imageSharingEffectHandler = DrawingEffectHandler {
        send(RtcMsg.ImageSharingContainer(it))
    }
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

    fun invokeCallMsg(msg: Msg) {
        listener?.invoke(TopLevelMsg.CallMsg(msg))
    }

    init {
        val webRtcManagerListener = object : WebRtcManagerI.Listener, Closeable {
            lateinit var handlerRef: CallEffectHandler

            private val closed = AtomicRef(false)
            private val handler: CallEffectHandler?
                get() = if (closed.value) null else handlerRef

            override fun close() {
                println("webRtcManagerListener set close")
                closed.value = true
            }

            override fun updateCaptureDimensions(dimensions: Size) {
                invokeCallMsg(Msg.UpdateLocalCaptureDimensions(dimensions))
            }

            override fun updateRemoveVideoContext(viewContext: VideoViewContext?) {
                viewContext?.let {
                    handler?.invokeCallMsg(
                        Msg.UpdateRemoteVideoContext(viewContext)
                    )
                }
            }

            override fun addCandidate(candidate: WebSocketMessage.Candidate) {
                coroutineScope.launch {
                    handler?.candidates?.emit(candidate)
                }
            }

            override fun sendOffer(webRTCSessionDescriptor: String) {
                handler?.send(WebSocketMessage.Offer(webRTCSessionDescriptor))
            }

            override fun sendAnswer(webRTCSessionDescriptor: String) {
                handler?.send(WebSocketMessage.Answer(webRTCSessionDescriptor))
            }

            override fun dataChannelStateChanged(state: DataChannelState) {
                println("dataChannelStateChanged $state")
                handler?.invokeCallMsg(
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
        addCloseableChild(webRtcManagerListener)
        addCloseableChild(
            coroutineScope.launch {
                candidatesSendState
                    .collect { shouldSend ->
                        candidatesSendCloseable.value =
                            if (shouldSend)
                                runWebRtcManager {
                                    candidates.collect {
                                        networkManager.send(it)
                                    }
                                }.asCloseable()
                            else null
                    }
            }
                .asCloseable()
        )
        coroutineScope.launch {
            addCloseableChild(
                networkManager.addListener(::listenWebSocketMessage)
            )
        }
        addCloseableChild(webRtcManager)
    }

    @Suppress("NAME_SHADOWING")
    override fun handleEffect(eff: TopLevelEff) {
        when (eff) {
            is TopLevelEff.CallEff -> {
                @Suppress("MoveVariableDeclarationIntoWhen")
                val eff = eff.eff
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
            else -> Unit
        }
    }

    override fun setListener(listener: suspend (TopLevelMsg) -> Unit) {
        super.setListener(listener)
        runWebRtcManager {
            invokeCallMsg(
                Msg.UpdateLocalVideoContext(
                    localVideoContext
                )
            )
        }
    }

    private fun initiateCall(userId: UserId) =
        send(
            WebSocketMessage.InitiateCall(
                userId,
            )
        )

    fun send(msg: WebSocketMessage) =
        msg.freeze()
            .let {
                coroutineScope.launch {
                    networkManager.send(msg)
                }
            }
            .also {
                println("CallEffectHandler send ws $msg")
            }

    private fun send(msg: RtcMsg) =
        runWebRtcManager {
            msg.freeze()
            dataChannelChunkManager
                .splitByteArrayIntoChunks(
                    when (msg) {
                        is RtcMsg.ImageSharingContainer -> {
                            when (msg.msg) {
                                is ImgSharingUpdateImage -> {
                                    RawDataMsgId.Image.wrapByteArray(msg.msg.imageData)
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
            println("CallEffectHandler send dt $msg")
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
        println("CallEffectHandler got dt $msg")
        when (msg) {
            is RtcMsg.ImageSharingContainer -> {
                imageSharingEffectHandler
                    .handleDataChannelMessage(msg.msg)
                    ?.let {
                        listener?.invoke(it)
                    }
            }
            is RtcMsg.UpdateDeviceState -> {
                invokeCallMsg(Msg.UpdateRemoteDeviceState(msg.deviceState))
            }
            is RtcMsg.UpdateViewPoint -> {
                invokeCallMsg(Msg.RemoteUpdateViewPoint(
                    when(msg.viewPoint) {
                        State.ViewPoint.Both -> State.ViewPoint.Both
                        State.ViewPoint.Mine -> State.ViewPoint.Partner
                        State.ViewPoint.Partner -> State.ViewPoint.Mine
                    }
                ))
            }
            is RtcMsg.UpdateCaptureDimensions -> {
                println("got UpdateCaptureDimensions")
                invokeCallMsg(Msg.UpdateRemoteCaptureDimensions(msg.dimensions))
            }
        }
    }

    private fun listenWebSocketMessage(msg: WebSocketMessage) = runWebRtcManager {
        when (msg) {
            is WebSocketMessage.Offer -> {
                invokeCallMsg(Msg.UpdateStatus(State.Status.Connecting))
                acceptOffer(msg.sessionDescriptor)
            }
            is WebSocketMessage.Answer -> {
                acceptAnswer(msg.sessionDescriptor)
            }
            is WebSocketMessage.Candidate -> {
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

