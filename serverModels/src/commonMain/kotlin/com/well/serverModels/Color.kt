package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 0xFF
) {
    val rgba: Long
        get() = alpha.toLong() or
                blue.toLong().shl(8) or
                green.toLong().shl(16) or
                red.toLong().shl(24)


    val argb: Long
        get() = blue.toLong() or
                green.toLong().shl(8) or
                red.toLong().shl(16) or
                alpha.toLong().shl(24)

    constructor(colorRGB: Long, alpha: Int = 0xFF) : this(
        red = (colorRGB.shr(16) and 0xFF).toInt(),
        green = (colorRGB.shr(8) and 0xFF).toInt(),
        blue = (colorRGB.shr(0) and 0xFF).toInt(),
        alpha = alpha
    )
}
