package com.well.androidApp.ui.composableScreens.theme

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color

object Color {
    val Pink = Color(0xF61F46)
    val Green = Color(0x94C83D)

    val CallGradient = listOf(Color(0x1A8B9D), Color(0x1B3D6D))

    private fun Color(hex: Int) = Color(color = hex.toLong() or 0xFF000000)
}