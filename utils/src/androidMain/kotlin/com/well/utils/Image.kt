package com.well.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.well.serverModels.Size
import java.io.ByteArrayOutputStream

actual class Image {
    val bitmap: Bitmap

    constructor(bitmap: Bitmap) {
        this.bitmap = bitmap
    }

    actual constructor(path: String) : this(BitmapFactory.decodeFile(path))

    actual val size: Size
        get() = Size(bitmap.width.toDouble(), bitmap.height.toDouble())

    actual fun resized(targetSize: Size) =
        Image(
            Bitmap.createScaledBitmap(
                bitmap,
                targetSize.width.toInt(),
                targetSize.height.toInt(),
                true
            )
        )

    actual fun encodeBase64(): String {
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        return Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
    }
}

actual fun String.decodeBase64Image(): Image {
    val byteArray = Base64.decode(this, Base64.DEFAULT)
    return Image(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.count()))
}