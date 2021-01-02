package com.well.sharedMobile.puerh.call

import com.well.serverModels.UserId
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.networking.webSocketManager.NetworkManager
import com.well.utils.EffectHandler
import com.well.utils.asCloseable
import com.well.utils.atomic.AtomicCloseableRef
import com.well.utils.freeze
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WebRtcEffectHandler(
    private val networkManager: NetworkManager,
    webRtcManagerGenerator: (List<String>, WebRtcManagerI.Listener) -> WebRtcManagerI,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<CallFeature.Eff, CallFeature.Msg>(coroutineScope) {

    private val candidatesSendState = networkManager
        .state
        .map { it == NetworkManager.Status.Connected }
        .distinctUntilChanged()
    private val candidatesSendCloseable = AtomicCloseableRef()
    private val webRtcManager: WebRtcManagerI

    init {
        val webRtcManagerListener = object : WebRtcManagerI.Listener {
            lateinit var handler: WebRtcEffectHandler

            override fun updateRemoveVideoContext(viewContext: VideoViewContext?) {
                handler.listener?.apply {
                    invoke(CallFeature.Msg.UpdateStatus(CallFeature.State.Status.Ongoing))
                    viewContext?.let {
                        invoke(
                            CallFeature.Msg.UpdateRemoteVideoContext(viewContext)
                        )
                    }
                }
            }

            override fun sendOffer(webRTCSessionDescriptor: String) {
                handler.send(WebSocketMessage.Offer(webRTCSessionDescriptor))
            }

            override fun sendAnswer(webRTCSessionDescriptor: String) {
                handler.send(WebSocketMessage.Answer(webRTCSessionDescriptor))
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
            candidates.freeze()
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
    }

    override fun handleEffect(eff: CallFeature.Eff) {
        when (eff) {
            is CallFeature.Eff.Initiate ->
                initiateCall(eff.userId)
            is CallFeature.Eff.Accept -> {
                runWebRtcManager {
                    sendOffer()
                }
            }
            CallFeature.Eff.End -> close()
        }
    }

    override fun setListener(listener: suspend (CallFeature.Msg) -> Unit) {
        super.setListener(listener)
        runWebRtcManager {
            listener.invoke(
                CallFeature.Msg.UpdateLocalVideoContext(
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

    private fun listenWebSocketMessage(msg: WebSocketMessage) = runWebRtcManager {
        when (msg) {
            is WebSocketMessage.Offer -> {
                listener?.invoke(CallFeature.Msg.UpdateStatus(CallFeature.State.Status.Connecting))
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
