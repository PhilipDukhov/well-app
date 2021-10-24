package com.well.modules.features.call.callFeature.webRtc

import com.well.modules.models.Size
import com.well.modules.models.WebSocketMsg
import com.well.modules.features.call.callFeature.VideoViewContext
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
        fun addCandidate(candidate: WebSocketMsg.Call.Candidate)
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
    fun acceptCandidate(candidate: WebSocketMsg.Call.Candidate)
    fun sendData(data: ByteArray)
    fun syncDeviceState(deviceState: LocalDeviceState)
}