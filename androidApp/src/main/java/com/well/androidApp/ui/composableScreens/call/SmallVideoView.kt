package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(45.dp)
                    .clickable(
                        onClick = onFlip,
                        interactionState = remember { InteractionState() },
                        indication = rememberRipple(
                            radius = 22.dp,
                        ),
                    )
            ) {
                Image(vectorResource(R.drawable.ic_flip_cam), contentDescription = null)
            }
        }
    }
}