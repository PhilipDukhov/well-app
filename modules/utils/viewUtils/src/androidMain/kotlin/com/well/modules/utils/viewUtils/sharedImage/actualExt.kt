package com.well.modules.utils.viewUtils.sharedImage

import android.graphics.Bitmap
import android.graphics.BitmapFactory

actual fun ByteArray.asImageContainer() = ImageContainer(asBitmap())

fun ByteArray.asBitmap(): Bitmap =
    BitmapFactory.decodeByteArray(
        this,
        0,
        count()
    )