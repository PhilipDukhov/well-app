package com.well.modules.utils.viewUtils.sharedImage

import com.well.modules.models.Size
import com.well.modules.models.toCGSize
import com.well.modules.models.toSize
import com.well.modules.utils.viewUtils.toByteArray
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

@Suppress("MemberVisibilityCanBePrivate")
actual class ImageContainer(val uiImage: UIImage) : SharedImage() {
    constructor(path: String) : this(UIImage.imageWithContentsOfFile(path)!!)

    actual val size = uiImage.size.useContents { toSize() }

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