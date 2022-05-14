package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
class Screen(
    val hostId: String,
    val imageURL: String? = null,
    val paths: List<DrawingPath> = listOf(),
)