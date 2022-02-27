package com.well.modules.models

import com.well.modules.models.formatters.initialsFromName
import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(
    val uid: User.Id,
    val review: Review,
)

@Serializable
data class ExistingReview(
    val userFullName: String,
    val profileImageUrl: String? = null,
    val review: Review,
) {
    val initials = userFullName.initialsFromName()
}

@Serializable
data class Review(
    val value: Int,
    val text: String? = null,
)