package com.well.sharedMobile.puerh.call

// Any type is a hack to decrease build time
actual data class VideoViewContext(val videoTrackAny: Any) {
    override fun toString(): String = "VideoViewContext"
}
