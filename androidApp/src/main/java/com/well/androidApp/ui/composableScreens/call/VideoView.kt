package com.well.androidApp.ui.composableScreens.call

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.well.androidApp.call.webRtc.TextureViewRenderer
import com.well.sharedMobile.puerh.call.VideoViewContext

@Composable
fun VideoView(
    context: VideoViewContext,
    modifier: Modifier,
) {
    val composableContext = LocalContext.current
    val customView = remember {
        TextureViewRenderer(composableContext).apply {
            init(context.eglBase.eglBaseContext, null)
            setEnableHardwareScaler(true)
//            setMirror(false)
        }
    }

    AndroidView(
        { customView },
        modifier = modifier
    ) {
        try {
            context.videoTrack.addSink(it)
        } catch (t: Throwable) { }
    }
    DisposableEffect(context.videoTrack.id()) {
        onDispose {
            try {
                context.videoTrack.removeSink(customView)
            } catch (t: Throwable) { }
        }
    }
}
