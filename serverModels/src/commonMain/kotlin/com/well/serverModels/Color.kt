package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Int = 0xFF
) {
    val rgba: Long = alpha.toLong() or
            blue.toLong().shl(8) or
            green.toLong().shl(16) or
            red.toLong().shl(24)

    val argb: Long = blue.toLong() or
            green.toLong().shl(8) or
            red.toLong().shl(16) or
            alpha.toLong().shl(24)

//    constructor(colorRGBA: Long) : this(
//        red = (colorRGBA.shr(24) and 0xFF).toInt(),
//        green = (colorRGBA.shr(16) and 0xFF).toInt(),
//        blue = (colorRGBA.shr(8) and 0xFF).toInt(),
//        alpha = (colorRGBA.shr(0) and 0xFF).toInt()
//    )

    constructor(colorRGB: Long) : this(
        red = (colorRGB.shr(16) and 0xFF).toInt(),
        green = (colorRGB.shr(8) and 0xFF).toInt(),
        blue = (colorRGB.shr(0) and 0xFF).toInt()
    )
}
