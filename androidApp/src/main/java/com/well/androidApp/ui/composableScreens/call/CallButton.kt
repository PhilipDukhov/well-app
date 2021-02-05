package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.theme.Color.Green
import com.well.androidApp.ui.composableScreens.theme.Color.Pink
import com.well.sharedMobile.ViewConstants
import com.well.sharedMobile.ViewConstants.CallScreen.CallButtonRadius

@Composable
fun CallUpButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = CallButton(
    R.drawable.ic_phone_up,
    Green,
    modifier,
    onClick,
)

@Composable
fun CallDownButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = CallButton(
    R.drawable.ic_phone_down,
    Pink,
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
        .background(background)
        .clickable(onClick = onClick)
) {
    Image(vectorResource(drawable), contentDescription = null)
}