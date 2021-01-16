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

    constructor(
        colorRGB: Long,
        alpha: Int = 0xFF
    ) : this(
        red = (colorRGB.shr(16) and 0xFF).toInt(),
        green = (colorRGB.shr(8) and 0xFF).toInt(),
        blue = (colorRGB.shr(0) and 0xFF).toInt(),
        alpha = alpha
    )

    companion object {
        val radicalRed = Color(0xFF2968)
        val pizazz = Color(0xFF9503)
        val supernova = Color(0xFFCC03)
        val atlantis = Color(0x63DA38)
        val dodgerBlue = Color(0x1BADF8)
        val lavender = Color(0xCC73E1)
        val black = Color(0xFFFFFF)
        val white = Color(0x000000)
        val drawingColors = listOf(
            radicalRed,
            pizazz,
            supernova,
            atlantis,
            dodgerBlue,
            lavender,
            black,
            white,
        )
    }
}
