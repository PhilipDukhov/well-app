package com.well.androidApp.ui.videoCall

import android.util.Log
import org.webrtc.*

open class PeerConnectionObserver : PeerConnection.Observer {
    val TAG = "PeerConnectionObserver"

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.e(TAG, "onIceCandidate: ")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Log.e(TAG, "onAddStream: " + mediaStream.videoTracks.size)
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Log.e(TAG, "onIceCandidatesRemoved: ")
    }

    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {
        Log.e(TAG, "onSignalingChange: ")
    }

    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
        Log.e(TAG, "onIceConnectionChange: ")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Log.e(TAG, "onIceConnectionReceivingChange: ")
    }

    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {
        Log.e(TAG, "onIceGatheringChange: ")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.e(TAG, "onRemoveStream: ")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.e(TAG, "onDataChannel: ")
    }

    override fun onRenegotiationNeeded() {
        Log.e(TAG, "onRenegotiationNeeded: ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.e(TAG, "onAddTrack: $p1")
    }
}