package com.well.sharedMobile.utils

import com.well.serverModels.Color
import platform.UIKit.UIColor

@Suppress("unused")
fun Color.toUIColor() =
    UIColor(
        red = red.toDouble() / 0xFF,
        green = green.toDouble() / 0xFF,
        blue = blue.toDouble() / 0xFF,
        alpha = alpha.toDouble() / 0xFF
    )