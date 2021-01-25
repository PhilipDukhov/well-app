package com.well.androidApp.call.webRtc

import com.github.aakira.napier.Napier
import org.webrtc.DataChannel

open class DataChannelObserver(
    private val dataChannel: DataChannel,
    name: String
): DataChannel.Observer {
    private val tag = "${this.javaClass.simpleName} $name"

    override fun onBufferedAmountChange(p0: Long) {
        Napier.d("onBufferedAmountChange: $p0", tag = tag)
    }

    override fun onStateChange() {
        onStateChange(dataChannel.state())
    }

    open fun onStateChange(state: DataChannel.State) {
        Napier.d("onStateChange $state", tag = tag)
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        Napier.d("onMessage: $p0", tag = tag)
    }
}