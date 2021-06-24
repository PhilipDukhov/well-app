package com.well.modules.utils.sharedImage

import com.well.modules.utils.toNSData
import platform.UIKit.UIImage

actual fun ByteArray.asImageContainer(): ImageContainer =
    ImageContainer(
        UIImage(
            toNSData()
        )
    )