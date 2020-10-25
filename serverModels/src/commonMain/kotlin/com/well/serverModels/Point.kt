package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Point(
    val x: Float,
    val y: Float,
)