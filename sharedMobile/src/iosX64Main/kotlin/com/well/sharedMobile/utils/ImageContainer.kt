package com.well.sharedMobile.utils

import com.well.serverModels.Size
import com.well.serverModels.toCGSize
import com.well.serverModels.toSize
import platform.CoreGraphics.CGRectMake
import platform.Foundation.*
import platform.UIKit.*

actual class ImageContainer(val uiImage: UIImage) {
    constructor(path: String) : this(UIImage.imageWithContentsOfFile(path)!!)

    actual val size = uiImage.size.toSize()

    actual fun resized(targetSize: Size): ImageContainer {
        val rect = CGRectMake(0.0, 0.0, targetSize.width, targetSize.height)
        UIGraphicsBeginImageContextWithOptions(targetSize.toCGSize(), false, 1.0)
        uiImage.drawInRect(rect)
        val newImage = UIGraphicsGetImageFromCurrentImageContext()!!
        UIGraphicsEndImageContext()
        return ImageContainer(newImage)
    }

    actual fun encodeBase64(): String =
        UIImagePNGRepresentation(uiImage)!!.base64Encoding()
}

@Suppress("CAST_NEVER_SUCCEEDS", "EXPERIMENTAL_UNSIGNED_LITERALS")
actual fun String.decodeBase64Image() =
    ImageContainer(
        UIImage(
            NSData.create(
                base64EncodedString = this,
                options = 0
            )!!
        )
    )