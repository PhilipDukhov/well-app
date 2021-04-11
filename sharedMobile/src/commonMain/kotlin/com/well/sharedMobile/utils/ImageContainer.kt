package com.well.sharedMobile.utils

import com.well.modules.models.Size
import com.well.modules.models.User

expect sealed class SharedImage

expect class UrlImage(url: String): SharedImage

fun User.profileImage(): SharedImage? = profileImageUrl?.let(::UrlImage)

expect class ImageContainer: SharedImage {
    val size: Size
    fun resized(targetSize: Size): ImageContainer
    fun asByteArray(compressionQuality: Float): ByteArray
}

expect fun ByteArray.asImageContainer(): ImageContainer
