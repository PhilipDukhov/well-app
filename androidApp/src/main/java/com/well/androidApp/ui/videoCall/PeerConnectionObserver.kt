package com.well.androidApp.ui.videoCall

import android.util.Log
import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer {
    private val tag = "PeerConnectionObserver"

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.e(tag, "onIceCandidate: ")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Log.e(tag, "onAddStream: " + mediaStream.videoTracks.size)
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Log.e(tag, "onIceCandidatesRemoved: ")
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Log.e(tag, "onSignalingChange: ")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.e(tag, "onIceConnectionChange: ")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Log.e(tag, "onIceConnectionReceivingChange: ")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Log.e(tag, "onIceGatheringChange: ")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.e(tag, "onRemoveStream: ")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.e(tag, "onDataChannel: ")
    }

    override fun onRenegotiationNeeded() {
        Log.e(tag, "onRenegotiationNeeded: ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.e(tag, "onAddTrack: $p1")
    }
}