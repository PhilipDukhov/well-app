package com.well.androidApp.ui.composableScreens.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Color

val colors = lightColors(
    primary = Color.Green.toColor(),
    onPrimary = Color.Black.toColor(),
    onSecondary = Color.White.toColor()
)

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}
