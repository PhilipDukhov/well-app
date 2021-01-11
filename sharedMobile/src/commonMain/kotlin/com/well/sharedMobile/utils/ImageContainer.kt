package com.well.sharedMobile.utils

import com.well.serverModels.Size

expect class ImageContainer {
    val size: Size
    fun resized(targetSize: Size): ImageContainer
    fun encodeBase64(): String
}

expect fun String.decodeBase64Image(): ImageContainer