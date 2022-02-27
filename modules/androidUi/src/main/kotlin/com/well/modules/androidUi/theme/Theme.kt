package com.well.modules.androidUi.theme

import com.well.modules.androidUi.ext.toColor
import com.well.modules.models.Color
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

val colors = lightColors(
    primary = Color.Green.toColor(),
    onPrimary = Color.Black.toColor(),
    onSecondary = Color.White.toColor(),
    error = Color.RadicalRed.toColor(),
    onError = Color.White.toColor()
)

@Composable
fun Theme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = MaterialTheme.shapes
            .copy(medium = RoundedCornerShape(14.dp))
    ) {
        CompositionLocalProvider(
            LocalOverScrollConfiguration provides null,
            content = content
        )
    }
}
