package com.well.sharedMobile.utils

import android.graphics.BitmapFactory

actual fun ByteArray.asImageContainer(): ImageContainer =
    ImageContainer(
        BitmapFactory.decodeByteArray(
            this,
            0,
            count()
        )
    )