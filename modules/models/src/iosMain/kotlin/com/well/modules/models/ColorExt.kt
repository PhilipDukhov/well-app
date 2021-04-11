package com.well.modules.models

import platform.UIKit.UIColor

fun Color.toUIColor() =
    UIColor(
        red = red.toDouble() / 0xFF,
        green = green.toDouble() / 0xFF,
        blue = blue.toDouble() / 0xFF,
        alpha = alpha.toDouble() / 0xFF
    )