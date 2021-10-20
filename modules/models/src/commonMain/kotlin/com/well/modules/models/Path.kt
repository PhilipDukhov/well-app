package com.well.modules.models

import com.well.modules.models.date.Date
import kotlinx.serialization.Serializable

@Serializable
data class Path(
    val points: List<Point>,
    val color: Color,
    val lineWidth: Float,
    val date: Date,
)