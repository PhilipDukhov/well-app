package com.well.androidApp.ui.composableScreens.call.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.customViews.Control
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.State
import com.google.accompanist.insets.statusBarsPadding

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
            size = smallControlSize,
        ) { listener(Msg.Undo) }
        Control(
            R.drawable.ic_redo,
            enabled = state.redoAvailable,
            size = smallControlSize,
        ) { listener(Msg.Redo) }
    }
    Row(modifier = Modifier.align(Alignment.CenterEnd)) {
        Control(
            R.drawable.ic_sf_trash_circle,
            size = smallControlSize,
        ) { listener(Msg.LocalClear(saveHistory = true)) }
        Control(
            R.drawable.ic_sf_photo,
            size = smallControlSize,
        ) { listener(Msg.RequestImageUpdate) }
    }
}

private val smallControlSize = 25.dp