package com.well.modules.features.call.callFeature

// Any type is a hack to decrease build time
actual class VideoViewContext(val videoTrackAny: Any) {
    override fun toString(): String = "VideoViewContext"
}