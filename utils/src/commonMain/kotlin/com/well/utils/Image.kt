package com.well.utils

import com.well.serverModels.Size

expect class Image(path: String) {
    val size: Size
    fun resized(targetSize: Size): Image
    fun encodeBase64(): String
}

expect fun String.decodeBase64Image(): Image