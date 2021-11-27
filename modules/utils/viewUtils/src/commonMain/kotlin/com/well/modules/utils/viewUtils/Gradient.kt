package com.well.modules.utils.viewUtils

import com.well.modules.models.Color
import com.well.modules.models.Point

data class Gradient internal constructor(
    val backgroundColor: Color = Color.Transparent,
    val overlayOpacity: Float = 1f,
    val stops: List<Stop>,
    val startPoint: Point,
    val endPoint: Point,
) {
    companion object {
        val Welcome = linearVertical(
            Stop(color = Color(colorRGB = 0x000000, alpha = 0.35f), location = 0f),
            Stop(color = Color(colorRGB = 0x0B121B, alpha = 0.53f), location = 1f),
        )

        val Login = linearVertical(
            Stop(color = Color(colorRGB = 0x000000, alpha = 0.26f), location = 0f),
            Stop(color = Color(colorRGB = 0x000000, alpha = 0.058f), location = 0.0729f),
            Stop(color = Color(colorRGB = 0x000000, alpha = 0f), location = 0.64f),
            Stop(color = Color(colorRGB = 0x111D2D, alpha = 0.65f), location = 1f),
        )

        val ActionButton = backgroundOverlay(
            backgroundColor = Color(colorRGB = 0x94C83D),
            overlayOpacity = 0.4f,
            stops = listOf(
                Stop(color = Color(colorRGB = 0x85D6F5, alpha = 0.35f), location = 0.0f),
                Stop(color = Color(colorRGB = 0x009BFF), location = 1f),
            ),
            startPoint = Point(
                x = 2.75387f,
                y = 2.43233f,
            ),
            endPoint = Point(
                x = 1.51096f,
                y = 3.01681f,
            ),
        )

        val Main = backgroundOverlay(
            backgroundColor = Color(colorRGB = 0x94C83D),
            overlayOpacity = 0.5f,
            stops = listOf(
                Stop(color = Color(colorRGB = 0x1BFFE4, alpha = 0.812f), location = 0.0f),
                Stop(color = Color(colorRGB = 0x009BFF), location = 0.672f),
            ),
            startPoint = Point(
                x = 0.04440333f,
                y = -0.20859465f,
            ),
            endPoint = Point(
                x = 0.72710454f,
                y = 1.2629647f,
            ),
        )



        val NavBar = backgroundOverlay(
            backgroundColor = Color(colorRGB = 0x94C83D),
            overlayOpacity = 0.5f,
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
            ),
        )

        val CallBackground = backgroundOverlay(
            backgroundColor = Color(colorRGB = 0x1B3D6D),
            stops = listOf(
                Stop(color = Color(colorRGB = 0x1B3D6D), location = 0.109375f),
                Stop(color = Color(colorRGB = 0x1BFFE4, alpha = 0.8f), location = 0.984375f),
            ),
            startPoint = Point(
                x = 1.1673f,
                y = 0.841f,
            ),
            endPoint = Point(
                x = -0.382f,
                y = -0.39f,
            )
        )

        val CallBottomBar = backgroundOverlay(
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

        private fun backgroundOverlay(
            backgroundColor: Color,
            overlayOpacity: Float = 0.5f,
            stops: List<Stop>,
            startPoint: Point,
            endPoint: Point,
        ) = Gradient(
            backgroundColor = backgroundColor,
            overlayOpacity = overlayOpacity,
            stops = stops,
            startPoint = startPoint,
            endPoint = endPoint
        )

        private fun linearVertical(
            vararg stops: Stop,
        ) = Gradient(
            backgroundColor = Color.Transparent,
            overlayOpacity = 1f,
            stops = stops.toList(),
            startPoint = Point(
                x = 0f,
                y = 0F,
            ),
            endPoint = Point(
                x = 0f,
                y = 1f,
            )
        )
    }

    data class Stop(
        val color: Color,
        val location: Float,
    )
}