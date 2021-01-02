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

@Composable
fun CallUpButton(onClick: () -> Unit) =
    CallButton(
        R.drawable.ic_phone_up,
        Green,
        onClick,
    )

@Composable
fun CallDownButton(onClick: () -> Unit) =
    CallButton(
        R.drawable.ic_phone_down,
        Pink,
        onClick
    )

@Composable
private fun CallButton(
    drawable: Int,
    background: Color,
    onClick: () -> Unit,
) =
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Image(vectorResource(drawable))
    }