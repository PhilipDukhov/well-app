package com.well.sharedMobile.puerh.call

import com.well.serverModels.WebSocketMessage
import com.well.utils.Closeable

interface WebRtcManagerI: Closeable {
    interface Listener {
        enum class DataChannelState {
            Connecting,
            Open,
            Closing,
            Closed,
        }
        fun updateRemoveVideoContext(viewContext: VideoViewContext?)
        fun addCandidate(candidate: WebSocketMessage.Candidate)
        fun sendOffer(webRTCSessionDescriptor: String)
        fun sendAnswer(webRTCSessionDescriptor: String)
        fun dataChannelStateChanged(state: DataChannelState)
        fun receiveData(data: ByteArray)
    }

    val localVideoContext: VideoViewContext
    fun sendOffer()
    fun acceptOffer(webRTCSessionDescriptor: String)
    fun acceptAnswer(webRTCSessionDescriptor: String)
    fun acceptCandidate(candidate: WebSocketMessage.Candidate)
    fun sendData(data: ByteArray)
}