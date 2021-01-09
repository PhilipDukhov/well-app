package com.well.sharedMobile.puerh.call

import com.well.serverModels.*
import com.well.sharedMobile.networking.webSocketManager.NetworkManager
import com.well.sharedMobile.puerh.call.CallFeature.Eff
import com.well.sharedMobile.puerh.call.CallFeature.Msg
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingEffectHandler
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature
import com.well.utils.EffectHandler
import com.well.utils.asCloseable
import com.well.utils.atomic.AtomicCloseableRef
import com.well.utils.freeze
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Eff as TopLevelEff
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.Msg as TopLevelMsg

class WebRtcEffectHandler(
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
    private val imageSharingEffectHandler = ImageSharingEffectHandler(
        ::send,
    )

    fun invokeCallMsg(msg: Msg) {
        listener?.invoke(TopLevelMsg.CallMsg(msg))
    }

    init {
        val webRtcManagerListener = object : WebRtcManagerI.Listener {
            lateinit var handler: WebRtcEffectHandler

            override fun updateRemoveVideoContext(viewContext: VideoViewContext?) {
                handler.invokeCallMsg(Msg.UpdateStatus(CallFeature.State.Status.Ongoing))
                viewContext?.let {
                    handler.invokeCallMsg(
                        Msg.UpdateRemoteVideoContext(viewContext)
                    )
                }
            }

            override fun addCandidate(candidate: WebSocketMessage.Candidate) {
                coroutineScope.launch {
                    handler.candidates.emit(candidate)
                }
            }

            override fun sendOffer(webRTCSessionDescriptor: String) {
                handler.send(WebSocketMessage.Offer(webRTCSessionDescriptor))
            }

            override fun sendAnswer(webRTCSessionDescriptor: String) {
                handler.send(WebSocketMessage.Answer(webRTCSessionDescriptor))
            }

            override fun receiveData(data: ByteArray) {
                handler.handleDataChannelMessage(data)
            }
        }
        webRtcManagerListener.handler = this
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
        webRtcManagerListener.freeze()
        addCloseableChild(webRtcManager)
    }

    override fun handleEffect(eff: TopLevelEff) {
        when (eff) {
            is TopLevelEff.CallEff -> {
                when (eff.eff) {
                    is Eff.Initiate ->
                        initiateCall(eff.eff.userId)
                    is Eff.Accept -> {
                        runWebRtcManager {
                            sendOffer()
                        }
                    }
                    Eff.End -> close()
                    Eff.StartImageSharing -> Unit
                    is Eff.UpdateDeviceState -> TODO()
                }
            }
            is TopLevelEff.ImageSharingEff -> {
                imageSharingEffectHandler.handleEffect(eff.eff)
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

    fun send(message: WebSocketMessage) =
        message.freeze()
            .let {
                coroutineScope.launch {
                    networkManager.send(message)
                }
            }

    private fun send(message: SharingStateDataChannelMessage) =
        runWebRtcManager {
            message.freeze()
            sendData(
                Json.encodeToString(
                    SharingStateDataChannelMessage.serializer(),
                    message,
                )
                    .encodeToByteArray()
            )
        }

    @Serializable
    sealed class SharingStateDataChannelMessage {
        data class InitiateSession(val date: Date) : SharingStateDataChannelMessage()
        object EndSession : SharingStateDataChannelMessage()
        data class UpdateViewSize(val size: Size) : SharingStateDataChannelMessage()
        data class UpdateImage(val imageData64String: String) : SharingStateDataChannelMessage()
        data class UpdatePaths(val paths: List<Path>) : SharingStateDataChannelMessage()
    }

    private fun handleDataChannelMessage(data: ByteArray) {
        val msg = Json.decodeFromString(
            SharingStateDataChannelMessage.serializer(),
            data.decodeToString()
        )
        imageSharingEffectHandler
            .handleDataChannelMessage(msg)
            ?.let {
                listener?.invoke(it)
            }
    }

    private fun listenWebSocketMessage(msg: WebSocketMessage) = runWebRtcManager {
        when (msg) {
            is WebSocketMessage.Offer -> {
                invokeCallMsg(Msg.UpdateStatus(CallFeature.State.Status.Connecting))
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

