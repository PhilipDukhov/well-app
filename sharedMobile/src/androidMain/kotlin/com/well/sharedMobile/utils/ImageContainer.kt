package com.well.sharedMobile.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Scale
import com.well.modules.models.Size
import java.io.ByteArrayOutputStream
import java.io.InputStream

actual sealed class SharedImage {
    abstract val coilDataAny: Any
}

actual data class UrlImage actual constructor(val url: String) : SharedImage() {
    override val coilDataAny: Any = url
}

actual class ImageContainer(private val content: Content) : SharedImage() {
    sealed class Content {
        data class Bitmap(val bitmap: android.graphics.Bitmap) : Content()
        data class Uri(
            val uri: android.net.Uri,
            val context: Context
        ) : Content() {
            fun inputStream(): InputStream = context.contentResolver.openInputStream(uri)!!
        }
    }

    constructor(
        uri: Uri,
        context: Context
    ) : this(Content.Uri(uri, context))

    constructor(bitmap: Bitmap) : this(Content.Bitmap(bitmap))

    actual val size: Size = when (content) {
        is Content.Bitmap -> {
            Size(content.bitmap.width, content.bitmap.height)
        }
        is Content.Uri -> {
            BitmapFactory.Options()
                .run {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeStream(content.inputStream(), null, this)
                    if (outWidth <= 0 || outHeight <= 0) {
                        throw IllegalStateException("decodeStream failed")
                    }
                    Size(outWidth, outHeight)
                }
        }
    }

    override val coilDataAny: Any
        get() = when (content) {
            is Content.Bitmap -> content.bitmap
            is Content.Uri -> content.uri
        }

    actual fun resized(targetSize: Size): ImageContainer {
        val widthPercentage = targetSize.width / size.width
        val heightPercentage = targetSize.height / size.height
        if (widthPercentage >= 1 && heightPercentage >= 1) {
            return this
        }

        return ImageContainer(
            Content.Bitmap(
                when (content) {
                    is Content.Bitmap -> {
                        if (size.width <= targetSize.width && size.height <= targetSize.width) {
                            content.bitmap
                        } else {
                            Bitmap.createScaledBitmap(
                                content.bitmap,
                                targetSize.width.toInt(),
                                targetSize.height.toInt(),
                                true
                            )!!
                        }
                    }
                    is Content.Uri -> {
                        content
                            .context
                            .imageLoader
                            .executeBlocking(
                                ImageRequest.Builder(content.context)
                                    .data(content.uri)
                                    .size(targetSize.width.toInt(), targetSize.height.toInt())
                                    .scale(Scale.FIT)
                                    .build()
                            )
                            .drawable!!
                            .toBitmap()
                    }
                }
            )
        )
    }

    actual fun asByteArray(compressionQuality: Float): ByteArray =
        when (content) {
            is Content.Bitmap -> {
                val output = ByteArrayOutputStream()
                content.bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    (compressionQuality * 100).toInt(),
                    output
                )
                output.toByteArray()
            }
            is Content.Uri -> {
                content
                    .inputStream()
                    .readBytes()
                    .asImageContainer()
                    .asByteArray(compressionQuality) //applying compressionQuality
            }
        }
}
