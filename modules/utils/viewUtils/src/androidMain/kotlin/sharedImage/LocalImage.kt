package com.well.modules.utils.viewUtils.sharedImage

import android.content.Context
import android.net.Uri

actual data class LocalImage(
    val uri: Uri,
    val context: Context,
) : SharedImage() {
    override val coilDataAny: Any = uri
    actual val path = uri.toString()

    actual fun toImageContainer(): ImageContainer = ImageContainer(uri = uri, context = context)
}