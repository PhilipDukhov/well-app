package com.well.modules.utils.sharedImage

import com.well.modules.models.Size

expect class ImageContainer: SharedImage {
    val size: Size
    fun resized(targetSize: Size): ImageContainer
    fun asByteArray(compressionQuality: Float): ByteArray
}