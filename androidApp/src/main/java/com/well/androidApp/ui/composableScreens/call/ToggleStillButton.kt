package com.well.androidApp.ui.composableScreens.call

import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.πExt.backgroundW
import com.well.androidApp.ui.composableScreens.πExt.borderW
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Color

@Composable
fun ToggleStillButton(
    vectorResourceId: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val size: Dp = 45.dp
    val activeColor = Color.White
    val inactiveColor = activeColor.withAlpha(0.3F)
    val backgroundColor: Color
    val borderWidth: Dp
    val borderColor: Color
    val tintColor: Color
    if (selected) {
        backgroundColor = inactiveColor
        borderWidth = 0.dp
        borderColor = Color.Transparent
        tintColor = activeColor
    } else {
        backgroundColor = Color.Transparent
        borderWidth = 2.dp
        borderColor = inactiveColor
        tintColor = inactiveColor
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clickable(
                onClick = onClick,
                interactionState = remember { InteractionState() },
                indication = rememberRipple(
                    radius = size / 2,
                )
            )
            .clip(CircleShape)
            .backgroundW(backgroundColor)
            .borderW(borderWidth, borderColor, shape = CircleShape)
    ) {
        Icon(
            painter = painterResource(vectorResourceId),
            contentDescription = null,
            tint = tintColor.toColor()
        )
    }
}