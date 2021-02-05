package com.well.androidApp.ui.composableScreens.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.AndroidView
import com.well.androidApp.call.webRtc.TextureViewRenderer
import com.well.sharedMobile.puerh.call.VideoViewContext

@Composable
fun VideoView(
    videoViewContext: VideoViewContext,
    modifier: Modifier,
) {
    val context = AmbientContext.current
    val customView = remember {
        TextureViewRenderer(context).apply {
            init(videoViewContext.eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
            setMirror(true)
        }
    }

    AndroidView(
        { customView },
        modifier = modifier
    ) {
        try {
            videoViewContext.videoTrack.addSink(it)
        } catch (t: Throwable) { }
    }
    DisposableEffect(videoViewContext.videoTrack.id()) {
        onDispose {
            try {
                videoViewContext.videoTrack.removeSink(customView)
            } catch (t: Throwable) { }
        }
    }
}
