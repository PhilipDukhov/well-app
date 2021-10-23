package com.well.androidApp.ui.composableScreens.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.well.androidApp.call.webRtc.TextureViewRenderer
import com.well.modules.features.call.VideoViewContext
import io.github.aakira.napier.Napier

@Composable
fun VideoView(
    videoContext: VideoViewContext,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val customView = remember {
        TextureViewRenderer(context).apply {
            init(videoContext.eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
//            setMirror(false)
        }
    }

    AndroidView(
        { customView },
        modifier = modifier
    ) {
        try {
            videoContext.videoTrack.addSink(it)
        } catch (t: Throwable) {
            Napier.e("VideoView addSink failed", throwable = t)
        }
    }
    DisposableEffect(videoContext.videoTrack.id()) {
        onDispose {
            try {
                videoContext.videoTrack.removeSink(customView)
            } catch (t: Throwable) { }
        }
    }
}