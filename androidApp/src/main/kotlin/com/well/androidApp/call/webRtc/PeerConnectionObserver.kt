package com.well.androidApp.call.webRtc

import com.well.modules.napier.Napier
import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer {
    private val tag = this.javaClass.simpleName

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Napier.d("onIceCandidate: $iceCandidate", tag = tag)
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Napier.d("onAddStream: ${mediaStream.videoTracks.size}", tag = tag)
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Napier.d("onIceCandidatesRemoved: $iceCandidates", tag = tag)
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Napier.d("onSignalingChange: ", tag = tag)
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Napier.d("onIceConnectionChange: ", tag = tag)
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Napier.d("onIceConnectionReceivingChange: ", tag = tag)
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Napier.d("onIceGatheringChange: ", tag = tag)
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Napier.d("onRemoveStream: ", tag = tag)
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Napier.d("onDataChannel: ", tag = tag)
    }

    override fun onRenegotiationNeeded() {
        Napier.d("onRenegotiationNeeded", tag = tag)
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Napier.d("onAddTrack $p0 $p1", tag = tag)
    }
}