package com.well.modules.androidUi.composableScreens.call

import com.well.modules.androidUi.R
import com.well.modules.androidUi.components.Control
import com.well.modules.androidWebrtc.VideoTextureView
import com.well.modules.features.call.callFeature.CallFeature
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import io.github.aakira.napier.Napier

@Suppress("ModifierParameter")
@Composable
fun VideoViewContainer(
    view: CallFeature.State.VideoView,
    onFlip: (() -> Unit)?,
    contentModifier: Modifier,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .alpha(if (view.hidden) 0F else 1F)
            .onSizeChanged {
                // testing position of flip button
                Napier.i("onSizeChanged ${it.width} ${it.height}")
            }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = contentModifier
            ) {
                VideoTextureView(
                    view.context,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
            if (onFlip != null) {
                FlipCameraButton(onFlip)
            }
        }
    }
}

@Composable
fun FlipCameraButton(onFlip: (() -> Unit), modifier: Modifier = Modifier) {
    Control(onClick = onFlip, modifier = modifier) {
        Image(
            painterResource(R.drawable.ic_flip_cam),
            contentDescription = null,
        )
    }
}