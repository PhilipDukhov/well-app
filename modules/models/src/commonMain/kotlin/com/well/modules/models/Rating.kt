package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    val uid: UserId,
    val rating: Rating,
)

@Serializable
data class Rating(
    val value: Int,
    val text: String? = null,
)