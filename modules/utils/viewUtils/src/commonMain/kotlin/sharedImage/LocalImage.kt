package com.well.modules.utils.viewUtils.sharedImage

@Suppress("CanSealedSubClassBeObject")
expect class LocalImage : SharedImage {
    val path: String

    fun toImageContainer(): ImageContainer
}