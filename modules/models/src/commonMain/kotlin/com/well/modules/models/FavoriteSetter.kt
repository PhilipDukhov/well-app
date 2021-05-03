package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteSetter(val favorite: Boolean, val userId: UserId)
