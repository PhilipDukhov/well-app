package com.well.androidApp.ui.webRtc

import com.github.aakira.napier.Napier
import org.webrtc.DataChannel

open class DataChannelObserver: DataChannel.Observer {
    private val tag = this.javaClass.simpleName

    override fun onBufferedAmountChange(p0: Long) {
        Napier.d("onBufferedAmountChange: $p0", tag = tag)
    }

    override fun onStateChange() {
        Napier.d("onStateChange", tag = tag)
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        Napier.d("onMessage: $p0", tag = tag)
    }
}