package com.well.modules.utils.viewUtils.sharedImage

actual data class LocalImage(actual val path: String) : SharedImage() {
    actual fun toImageContainer(): ImageContainer = ImageContainer(path = path)
}