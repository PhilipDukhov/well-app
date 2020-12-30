package com.well.shared.puerh.call

import com.well.serverModels.WebSocketMessage
import kotlinx.coroutines.flow.SharedFlow

interface WebRtcManagerI {
    interface Listener {
        fun updateRemoveVideoContext(viewContext: SurfaceViewContext?)
        fun sendOffer(webRTCSessionDescriptor: String)
        fun sendAnswer(webRTCSessionDescriptor: String)
    }

    val localVideoContext: SurfaceViewContext
    val candidates: SharedFlow<WebSocketMessage.Candidate>
    fun sendOffer()
    fun acceptOffer(webRTCSessionDescriptor: String)
    fun acceptAnswer(webRTCSessionDescriptor: String)
    fun acceptCandidate(candidate: WebSocketMessage.Candidate)
}