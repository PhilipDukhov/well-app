package com.well.androidApp.ui.composableScreens.call.screenSharing

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
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.Msg
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.State
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun TopPanel(
    state: State,
    listener: (Msg) -> Unit,
    modifier: Modifier,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier = modifier
        .background(Color.Black)
        .statusBarsPadding()
) {
    UndoRedoButton(
        R.drawable.ic_undo,
        state.undoAvailable,
        onClick = { listener(Msg.Undo) },
    )
    UndoRedoButton(
        R.drawable.ic_redo,
        state.redoAvailable,
        onClick = { listener(Msg.Redo) },
    )
}

@Composable
private fun UndoRedoButton(
    vectorResourceId: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
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
        vectorResource(id = vectorResourceId),
        contentDescription = null,
        modifier = Modifier
            .padding(5.dp)
            .alpha(if (enabled) 1F else 0.4F)
    )
}

private const val size = 44