package com.well.androidApp.call.webRtc

import com.well.napier.Napier
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SimpleSdpObserver : SdpObserver {
    private val tag = this.javaClass.simpleName

    override fun onCreateSuccess(sessionDescription: SessionDescription) {
        Napier.d("onCreateSuccess: $sessionDescription", tag = tag)
    }

    override fun onSetSuccess() {
        Napier.d("onSetSuccess:", tag = tag)
    }

    override fun onCreateFailure(p0: String) {
        Napier.d("onCreateFailure: $p0", tag = tag)
    }

    override fun onSetFailure(p0: String) {
        Napier.d("onSetFailure: $p0", tag = tag)
    }
}