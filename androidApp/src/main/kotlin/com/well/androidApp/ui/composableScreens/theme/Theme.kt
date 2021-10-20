package com.well.androidApp.ui.composableScreens.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider

val colors = lightColors(
    primary = Color.Green.toColor(),
    onPrimary = Color.Black.toColor(),
    onSecondary = Color.White.toColor(),
)

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = colors,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalOverScrollConfiguration provides null,
            content = content
        )
    }
}
