package com.well.sharedMobile.puerh.call.webRtc

import com.well.modules.models.Size
import com.well.modules.models.WebSocketMessage
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.VideoViewContext
import com.well.modules.atomic.Closeable

interface WebRtcManagerI: Closeable {
    interface Listener {
        enum class DataChannelState {
            Connecting,
            Open,
            Closing,
            Closed,
        }
        fun updateCaptureDimensions(dimensions: Size)
        fun updateRemoveVideoContext(viewContext: VideoViewContext?)
        fun addCandidate(candidate: WebSocketMessage.Candidate)
        fun sendOffer(webRTCSessionDescriptor: String)
        fun sendAnswer(webRTCSessionDescriptor: String)
        fun dataChannelStateChanged(state: DataChannelState)
        fun receiveData(data: ByteArray)
    }

    val localVideoContext: VideoViewContext
    val manyCamerasAvailable: Boolean
    fun sendOffer()
    fun acceptOffer(webRTCSessionDescriptor: String)
    fun acceptAnswer(webRTCSessionDescriptor: String)
    fun acceptCandidate(candidate: WebSocketMessage.Candidate)
    fun sendData(data: ByteArray)
    fun syncDeviceState(deviceState: LocalDeviceState)
}