package com.well.modules.androidUi.composableScreens.call

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.well.modules.androidUi.R
import androidx.compose.foundation.Image
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.models.Color
import com.well.modules.viewHelpers.ViewConstants.CallScreen.CallButtonRadius

@Composable
fun CallUpButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = CallButton(
    R.drawable.ic_phone_up,
    Color.Green,
    modifier,
    onClick,
)

@Composable
fun CallDownButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = CallButton(
    R.drawable.ic_phone_down,
    Color.Pink,
    modifier,
    onClick,
)

@Composable
private fun CallButton(
    drawable: Int,
    background: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) = Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
        .size((CallButtonRadius * 2).dp)
        .clip(CircleShape)
        .backgroundKMM(background)
        .clickable(onClick = onClick)
) {
    Image(
        painterResource(drawable),
        contentDescription = null,
    )
}