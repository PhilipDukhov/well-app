package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
class FavoriteSetter(val favorite: Boolean, val uid: User.Id)
