package com.well.androidApp.ui.videoCall

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class SimpleSdpObserver : SdpObserver {
    override fun onCreateSuccess(sessionDescription: SessionDescription) {
    }

    override fun onSetSuccess() {
    }

    override fun onCreateFailure(p0: String) {
    }

    override fun onSetFailure(p0: String) {
    }
}