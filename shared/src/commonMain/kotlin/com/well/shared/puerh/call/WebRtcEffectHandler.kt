package com.well.shared.puerh.call

import com.well.serverModels.UserId
import com.well.serverModels.WebSocketMessage
import com.well.shared.puerh.WebSocketManager
import com.well.utils.Closeable
import com.well.utils.EffectHandler
import com.well.utils.asCloseable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WebRtcEffectHandler(
    private val webSocketManager: WebSocketManager,
    webRtcManagerGenerator: (WebRtcManagerI.Listener) -> WebRtcManagerI,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<CallFeature.Eff, CallFeature.Msg>(coroutineScope) {
    private val webRtcManager = webRtcManagerGenerator(
        object : WebRtcManagerI.Listener {
            override fun updateRemoveVideoContext(viewContext: SurfaceViewContext?) {
                listener?.apply {
                    invoke(CallFeature.Msg.UpdateStatus(CallFeature.State.Status.Ongoing))
                    viewContext?.let {
                        invoke(
                            CallFeature.Msg.UpdateRemoteVideoContext(viewContext)
                        )
                    }
                }
            }

            override fun sendOffer(webRTCSessionDescriptor: String) {
                send(WebSocketMessage.Offer(webRTCSessionDescriptor))
            }

            override fun sendAnswer(webRTCSessionDescriptor: String) {
                send(WebSocketMessage.Answer(webRTCSessionDescriptor))
            }
        }
    )

    private val candidatesSendState = webSocketManager
        .state
        .map { it == WebSocketManager.Status.Connected }
        .distinctUntilChanged()

    private var candidatesSendCloseable: Closeable? = null
        set(value) {
            field?.close()
            field = value
        }
    init {
        addCloseableChild(
            coroutineScope.launch {
                candidatesSendState
                    .collect { shouldSend ->
                        candidatesSendCloseable =
                            if (shouldSend)
                                coroutineScope.launch {
                                    webRtcManager.candidates.collect {
                                        println("listenWebSocketMessage send $it")
                                        webSocketManager.send(it)
                                    }
                                }.asCloseable()
                            else null
                    }
            }.asCloseable()
        )
    }

    init {
        coroutineScope.launch {
            addCloseableChild(
                webSocketManager.addListener(::listenWebSocketMessage)
            )
        }
    }

    override fun handleEffect(eff: CallFeature.Eff) {
        when (eff) {
            is CallFeature.Eff.Initiate ->
                initiateCall(eff.userId)
            is CallFeature.Eff.Accept -> {
                webRtcManager.sendOffer()
            }
            CallFeature.Eff.End -> close()
        }
    }

    override fun setListener(listener: suspend (CallFeature.Msg) -> Unit) {
        super.setListener(listener)
        coroutineScope.launch {
            listener.invoke(
                CallFeature.Msg.UpdateLocalVideoContext(
                    webRtcManager.localVideoContext
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
        coroutineScope.launch {
            webSocketManager.send(message)
        }

    private fun listenWebSocketMessage(msg: WebSocketMessage) {
        when (msg) {
            is WebSocketMessage.Offer -> {
                listener?.invoke(CallFeature.Msg.UpdateStatus(CallFeature.State.Status.Connecting))
                webRtcManager.acceptOffer(msg.sessionDescriptor)
            }
            is WebSocketMessage.Answer -> {
                webRtcManager.acceptAnswer(msg.sessionDescriptor)
            }
            is WebSocketMessage.Candidate -> {
                webRtcManager.acceptCandidate(msg)
            }
            else -> Unit
        }
    }
}