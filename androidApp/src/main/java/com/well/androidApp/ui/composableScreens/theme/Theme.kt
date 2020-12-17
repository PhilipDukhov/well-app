package com.well.androidApp.ui.composableScreens.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = Typography,
        content = content
    )
}
