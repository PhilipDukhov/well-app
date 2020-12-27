package com.well.shared.puerh.call

import org.webrtc.EglBase
import org.webrtc.VideoTrack

actual data class SurfaceViewContext(
    val eglBase: EglBase,
    val videoTrack: VideoTrack,
) {
    override fun toString(): String =
        "SurfaceViewContext(videoTrack=$videoTrack)"
}