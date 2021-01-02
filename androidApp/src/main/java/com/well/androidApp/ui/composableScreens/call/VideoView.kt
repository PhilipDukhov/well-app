package com.well.androidApp.ui.composableScreens.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.onDispose
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.viewinterop.AndroidView
import com.well.androidApp.ui.webRtc.TextureViewRenderer
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
        videoViewContext.videoTrack.addSink(it)
    }
    onDispose {
        videoViewContext.videoTrack.removeSink(customView)
    }
}
