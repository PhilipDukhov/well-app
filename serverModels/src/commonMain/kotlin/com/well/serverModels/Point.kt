package com.well.serverModels

import kotlinx.serialization.Serializable
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sqrt

@Serializable
data class Point(
    val x: Float,
    val y: Float,
) {
    fun intersects(
        point: Point,
        offset: Float
    ): Boolean = hypot(x - point.x, y - point.y) <= offset
}