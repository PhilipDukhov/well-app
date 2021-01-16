package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Path(
    val points: List<Point>,
    val color: Color,
    val lineWidth: Float,
)