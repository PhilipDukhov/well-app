package com.well.modules.features.call.callFeature

import org.webrtc.EglBase
import org.webrtc.VideoTrack

actual class VideoViewContext(
    val eglBase: EglBase,
    val videoTrack: VideoTrack,
) {
    override fun toString(): String =
        "VideoViewContext(videoTrack=$videoTrack)"
}