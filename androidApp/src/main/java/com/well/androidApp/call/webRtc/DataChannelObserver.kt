package com.well.androidApp.call.webRtc

import org.webrtc.DataChannel

open class DataChannelObserver(
    private val dataChannel: DataChannel,
    name: String
): DataChannel.Observer {
    private val tag = "${this.javaClass.simpleName} $name"

    override fun onBufferedAmountChange(p0: Long) {
        println("$tag onBufferedAmountChange: $p0")
    }

    override fun onStateChange() {
        onStateChange(dataChannel.state())
    }

    open fun onStateChange(state: DataChannel.State) {
        println("$tag onStateChange $state")
    }

    override fun onMessage(p0: DataChannel.Buffer?) {
        println("$tag onMessage: $p0")
    }
}