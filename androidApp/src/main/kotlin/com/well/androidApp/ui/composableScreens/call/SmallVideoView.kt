package com.well.androidApp.ui.composableScreens.call

import com.well.androidApp.R
import com.well.androidApp.ui.customViews.Control
import com.well.androidApp.ui.ext.Image
import io.github.aakira.napier.Napier
import com.well.modules.features.call.CallFeature
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource

@Suppress("ModifierParameter")
@Composable
fun VideoViewContainer(
    view: CallFeature.State.VideoView,
    onFlip: (() -> Unit)?,
    containerModifier: Modifier,
    modifier: Modifier,
) {
    Box(
        modifier = Modifier.background(Color.Red).then(containerModifier)
            .alpha(if (view.hidden) 0F else 1F)
            .onSizeChanged {
                // testing position of flip button
                Napier.i("onSizeChanged ${it.width} ${it.height}")
            }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = modifier
            ) {
                VideoView(
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
        Image(painterResource(R.drawable.ic_flip_cam))
    }
}