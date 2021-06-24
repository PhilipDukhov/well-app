package com.well.modules.utils.sharedImage

import com.well.modules.models.User

expect fun ByteArray.asImageContainer(): ImageContainer

fun User.profileImage(): SharedImage? = profileImageUrl?.let(::UrlImage)
