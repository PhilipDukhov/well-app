package com.well.androidApp.call.webRtc

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SimpleSdpObserver : SdpObserver {
    private val tag = this.javaClass.simpleName

    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        println("$tag onCreateSuccess: $sessionDescription")
    }

    override fun onSetSuccess() {
        println("$tag onSetSuccess:")
    }

    override fun onCreateFailure(p0: String) {
        println("$tag onCreateFailure: $p0")
    }

    override fun onSetFailure(p0: String) {
        println("$tag onSetFailure: $p0")
    }
}