package com.well.modules.androidWebrtc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.well.modules.features.call.callFeature.VideoViewContext
import io.github.aakira.napier.Napier

@Composable
fun VideoTextureView(
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