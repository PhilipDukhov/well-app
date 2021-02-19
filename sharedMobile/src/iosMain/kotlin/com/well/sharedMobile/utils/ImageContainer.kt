package com.well.sharedMobile.utils

import com.well.serverModels.Size
import com.well.serverModels.toCGSize
import com.well.serverModels.toSize
import com.well.sharedMobile.puerh.toByteArray
import com.well.sharedMobile.puerh.toNSData
import platform.CoreGraphics.CGRectMake
import platform.UIKit.*

actual sealed class SharedImage

actual data class UrlImage actual constructor(val url: String) : SharedImage()

@Suppress("MemberVisibilityCanBePrivate")
actual class ImageContainer(val uiImage: UIImage): SharedImage() {
    constructor(path: String) : this(UIImage.imageWithContentsOfFile(path)!!)

    actual val size = uiImage.size.toSize()

    actual fun resized(targetSize: Size): ImageContainer {
        val rect = CGRectMake(0.0, 0.0, targetSize.width.toDouble(), targetSize.height.toDouble())
        UIGraphicsBeginImageContextWithOptions(targetSize.toCGSize(), false, 1.0)
        uiImage.drawInRect(rect)
        val newImage = UIGraphicsGetImageFromCurrentImageContext()!!
        UIGraphicsEndImageContext()
        return ImageContainer(newImage)
    }

    actual fun asByteArray(compressionQuality: Float): ByteArray =
        UIImageJPEGRepresentation(uiImage, compressionQuality.toDouble())!!.toByteArray()
}

actual fun ByteArray.asImageContainer(): ImageContainer =
    ImageContainer(
        UIImage(
            toNSData()
        )
    )