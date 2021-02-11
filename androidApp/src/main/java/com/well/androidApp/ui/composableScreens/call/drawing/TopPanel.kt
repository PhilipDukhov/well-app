package com.well.androidApp.ui.composableScreens.call.drawing

import androidx.compose.foundation.Image
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.State
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun TopPanel(
    state: State,
    listener: (Msg) -> Unit,
    modifier: Modifier,
) = Box(
    modifier = modifier
        .background(Color.Black)
        .statusBarsPadding()
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .align(Alignment.Center)
    ) {
        Control(
            R.drawable.ic_undo,
            enabled = state.undoAvailable,
            onClick = { listener(Msg.Undo) },
        )
        Control(
            R.drawable.ic_redo,
            enabled = state.redoAvailable,
            onClick = { listener(Msg.Redo) },
        )
    }
    Control(
        R.drawable.ic_sf_photo,
        onClick = { listener(Msg.RequestImageUpdate) },
        modifier = Modifier
            .align(Alignment.CenterEnd)
    )
}

@Composable
private fun Control(
    vectorResourceId: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
        .clickable(
            enabled = enabled,
            interactionState = remember { InteractionState() },
            indication = rememberRipple(
                bounded = false,
                radius = (size / 2).dp,
            ),
            onClick = onClick,
        )
        .size(size.dp)
) {
    Image(
        painterResource(id = vectorResourceId),
        contentDescription = null,
        modifier = Modifier
            .padding(5.dp)
            .alpha(if (enabled) 1F else 0.4F)
    )
}

private const val size = 44