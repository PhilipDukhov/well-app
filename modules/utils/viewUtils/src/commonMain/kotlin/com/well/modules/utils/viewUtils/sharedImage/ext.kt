package com.well.modules.utils.viewUtils.sharedImage

import com.well.modules.models.Size

expect fun ByteArray.asImageContainer(): ImageContainer

fun ImageContainer.asByteArrayOptimizedForNetwork() : ByteArray {
    var data: ByteArray
    var quality = 1f
    do {
        data = resizedImage(Size(2000))
            .asByteArray(quality)
        quality -= 0.2f
    } while (data.count() > 250_000 && quality >= 0)
    return data
}