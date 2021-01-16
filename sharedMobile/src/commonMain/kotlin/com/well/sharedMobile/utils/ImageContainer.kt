package com.well.sharedMobile.utils

import com.well.serverModels.Size

expect class ImageContainer {
    val size: Size
    fun resized(targetSize: Size): ImageContainer
    fun asByteArray(): ByteArray
}

expect fun ByteArray.asImageContainer(): ImageContainer