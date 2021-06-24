package com.well.modules.utils.sharedImage

@Suppress("CanSealedSubClassBeObject")
expect class LocalImage : SharedImage {
    val path: String

    fun toImageContainer(): ImageContainer
}