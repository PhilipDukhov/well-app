package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class Path(
    val points: List<Point>,
    val color: Color,
    val lineWidth: Float,
    val date: Date,
)