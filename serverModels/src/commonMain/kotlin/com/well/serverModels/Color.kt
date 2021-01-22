package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Color(
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float = 1F,
) {
    val argb: Long by lazy {
        blue.toLong() or
            green.toLong().shl(8) or
            red.toLong().shl(16) or
            (alpha * 0xFF).toLong().shl(24)
    }

    constructor(
        colorRGB: Long,
        alpha: Float = 1F
    ) : this(
        red = (colorRGB.shr(16) and 0xFF).toInt(),
        green = (colorRGB.shr(8) and 0xFF).toInt(),
        blue = (colorRGB.shr(0) and 0xFF).toInt(),
        alpha = alpha
    )

    fun withAlpha(alpha: Float) = copy(alpha = alpha)

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        val MineShaft = Color(0x272727)
        val AzureRadiance = Color(0x037AFF)

        val RadicalRed = Color(0xFF2968)
        val Pizazz = Color(0xFF9503)
        val Supernova = Color(0xFFCC03)
        val Atlantis = Color(0x63DA38)
        val DodgerBlue = Color(0x1BADF8)
        val Lavender = Color(0xCC73E1)
        val Black = Color(0x000000)
        val White = Color(0xFFFFFF)
        val Transparent = Color(0x000000, 0F)

        val InactiveOverlay = Black.withAlpha(0.4F)

        val drawingColors = listOf(
            RadicalRed,
            Pizazz,
            Supernova,
            Atlantis,
            DodgerBlue,
            Lavender,
            Black,
            White,
        )
    }
}
