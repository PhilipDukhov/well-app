package com.well.sharedMobile.puerh.call

import com.well.serverModels.Size
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature

fun ImageSharingFeature.Eff.UploadImage.resizedImageBase64Encoding(): ByteArray {
    if (image.size.width <= remoteViewSize.width && image.size.height <= remoteViewSize.width) {
        return image.asByteArray()
    }
    val widthRatio = remoteViewSize.width / image.size.width
    val heightRatio = remoteViewSize.height / image.size.height
    val newSize = if (widthRatio > heightRatio) {
        Size(image.size.width * heightRatio, image.size.height * heightRatio)
    } else {
        Size(image.size.width * widthRatio, image.size.height * widthRatio)
    }
    return image.resized(newSize).asByteArray()
}