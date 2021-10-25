package com.well.modules.utils.viewUtils.sharedImage

import com.well.modules.utils.viewUtils.toNSData
import platform.UIKit.UIImage

actual fun ByteArray.asImageContainer(): ImageContainer =
    ImageContainer(
        UIImage(
            toNSData()
        )
    )