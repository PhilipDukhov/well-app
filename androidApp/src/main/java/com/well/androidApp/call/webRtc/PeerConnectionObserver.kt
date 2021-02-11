package com.well.androidApp.call.webRtc

import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer {
    private val tag = this.javaClass.simpleName

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        println("$tag onIceCandidate: $iceCandidate")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        println("$tag onAddStream: ${mediaStream.videoTracks.size}")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        println("$tag onIceCandidatesRemoved: $iceCandidates")
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        println("$tag onSignalingChange: ")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        println("$tag onIceConnectionChange: ")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        println("$tag onIceConnectionReceivingChange: ")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        println("$tag onIceGatheringChange: ")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        println("$tag onRemoveStream: ")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        println("$tag onDataChannel: ")
    }

    override fun onRenegotiationNeeded() {
        println("$tag onRenegotiationNeeded: ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        println("$tag onAddTrack: $p1")
    }
}