package com.well.modules.utils.sharedImage

import com.well.modules.models.User

fun User.profileImage(): SharedImage? = profileImageUrl?.let(::UrlImage)