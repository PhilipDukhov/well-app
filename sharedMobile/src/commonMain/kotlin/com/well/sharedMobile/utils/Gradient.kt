package com.well.sharedMobile.utils

import com.well.modules.models.Color
import com.well.modules.models.Point

data class Gradient(
    val backgroundColor: Color,
    val overlayOpacity: Float = 0.5F,
    val stops: List<Stop>,
    val startPoint: Point,
    val endPoint: Point,
) {
    companion object {
        val Main = Gradient(
            backgroundColor = Color(colorRGB = 0x94C83D),
            stops = listOf(
                Stop(color = Color(colorRGB = 0x1BFFE4, alpha = 0.8f), location = 0.0f),
                Stop(color = Color(colorRGB = 0x009BFF), location = 0.67f),
            ),
            startPoint = Point(
                x = -0.06529792748787552f,
                y = -1.2714285714285714F,
            ),
            endPoint = Point(
                x = 0.7983896522706257f,
                y = 0.9809522356305803f,
            )
        )

        val CallBackground = Gradient(
            backgroundColor = Color(colorRGB = 0x1B3D6D),
            stops = listOf(
                Stop(color = Color(colorRGB = 0x1B3D6D), location = 0.109375f),
                Stop(color = Color(colorRGB = 0x1BFFE4, alpha = 0.8f), location = 0.984375f),
            ),
            startPoint = Point(
                x = 1.1673333333333331f,
                y = 0.8408037094281299f,
            ),
            endPoint = Point(
                x = -0.3820000000000002f,
                y = -0.3898763523956724f,
            )
        )

        val CallBottomBar = Gradient(
            backgroundColor = Color(colorRGB = 0x1B3D6D),
            stops = listOf(
                Stop(color = Color(colorRGB = 0x1BFFE4, alpha = 0.8f), location = 0.109375f),
                Stop(color = Color(colorRGB = 0x1B3D6D), location = 0.984375f),
            ),
            startPoint = Point(
                x = 0.762648f,
                y = -0.376472566f,
            ),
            endPoint = Point(
                x = 0.812653333f,
                y = 0.803190265f,
            )
        )
    }

    data class Stop(
        val color: Color,
        val location: Float
    )
}