package com.well.modules.models

import kotlinx.serialization.Serializable
import kotlin.math.hypot

@Serializable
data class Point(
    val x: Float,
    val y: Float,
) {
    fun intersects(
        point: Point,
        offset: Float,
    ): Boolean = hypot(x - point.x, y - point.y) <= offset
}