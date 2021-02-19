package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.sharedMobile.puerh.call.CallFeature

@Suppress("ModifierParameter")
@Composable
fun VideoViewContainer(
    view: CallFeature.State.VideoView,
    onFlip: (() -> Unit)?,
    containerModifier: Modifier,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = containerModifier
            .alpha(if (view.hidden) 0F else 1F)
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

@Composable
fun FlipCameraButton(onFlip: (() -> Unit), modifier: Modifier = Modifier) {
    Control(onClick = onFlip, modifier = modifier) {
        Image(painterResource(R.drawable.ic_flip_cam))
    }
}