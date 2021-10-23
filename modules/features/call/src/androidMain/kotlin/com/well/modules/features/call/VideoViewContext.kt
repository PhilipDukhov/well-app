package com.well.modules.features.call

import org.webrtc.EglBase
import org.webrtc.VideoTrack

actual data class VideoViewContext(
    val eglBase: EglBase,
    val videoTrack: VideoTrack,
) {
    override fun toString(): String =
        "VideoViewContext(videoTrack=$videoTrack)"
}