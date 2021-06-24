package com.well.modules.utils.sharedImage

actual data class UrlImage actual constructor(val url: String) : SharedImage() {
    override val coilDataAny: Any = url
}