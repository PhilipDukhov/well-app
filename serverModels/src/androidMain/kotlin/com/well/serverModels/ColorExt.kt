package com.well.serverModels

import androidx.annotation.ColorInt

@ColorInt
fun Color.colorInt(): Int {
    return argb.toInt()
}
