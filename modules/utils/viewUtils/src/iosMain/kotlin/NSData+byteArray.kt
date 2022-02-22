package com.well.modules.utils.viewUtils

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

fun ByteArray.toNSData() : NSData = memScoped {
    NSData.create(
        bytes = allocArrayOf(this@toNSData),
        length = size.toULong()
    )
}

fun NSData.toByteArray() : ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }