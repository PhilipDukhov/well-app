package com.well.androidApp.ui.composableScreens.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

data class SurfaceViewContext(val eglBase: EglBase, val videoTrack: VideoTrack)

@Composable
fun SurfaceView(surfaceViewContext: SurfaceViewContext) {
    val context = AmbientContext.current
    val customView = remember {
        SurfaceViewRenderer(context).apply {
            init(surfaceViewContext.eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
            setMirror(true)
        }
    }

    AndroidView({ customView }) {
        println("SurfaceView addSink")
        surfaceViewContext.videoTrack.addSink(it)
    }
    onDispose {
        println("SurfaceView removeSink")
        surfaceViewContext.videoTrack.removeSink(customView)
    }
}
