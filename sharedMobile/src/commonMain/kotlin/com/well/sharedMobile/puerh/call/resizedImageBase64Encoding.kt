package com.well.sharedMobile.puerh.call

import com.well.serverModels.Size
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature
import com.well.sharedMobile.utils.ImageContainer

fun ImageContainer.resizedImage(maxSize: Size): ImageContainer {
    if (size.width <= maxSize.width && size.height <= maxSize.width) {
        return this
    }
    val widthRatio = maxSize.width / size.width
    val heightRatio = maxSize.height / size.height
    val newSize = if (widthRatio > heightRatio) {
        Size(size.width * heightRatio, size.height * heightRatio)
    } else {
        Size(size.width * widthRatio, size.height * widthRatio)
    }
    return resized(newSize)
}