package com.well.modules.models

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
        val inactiveAlpha = 0.38f

        val MineShaft = Color(0x272727)
        val AzureRadiance = Color(0x037AFF)
        val RadicalRed = Color(0xFF2968)
        val Pizazz = Color(0xFF9503)
        val Supernova = Color(0xFFCC03)
        val Atlantis = Color(0x63DA38)
        val DodgerBlue = Color(0x1BADF8)
        val Lavender = Color(0xCC73E1)

        val LightBlue = Color(0x6AA2B8)
        val LightBlue15 = Color(0x6AA2B8, alpha = 0.15f)
        val MediumBlue = Color(0x0064A4)
        val DarkBlue = Color(0x1B3D6D)
        val Green = Color(0x94C83D)
        val Green10 = Color(0x94C83D, alpha = 0.1f)
        val DarkGrey = Color(0x555759)
        val BlackP = Color(0x01070E)
        val LightGray = Color(0xC4C4C4)
        val Pink = Color(0xF61F46)

        val Black = Color(0x000000)
        val White = Color(0xFFFFFF)
        val Transparent = Color(0x000000, 0F)

        val InactiveOverlay = Black.withAlpha(inactiveAlpha)


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
