package com.well.modules.utils.viewUtils

import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.timeIntervalSinceNow
import platform.posix.uint8_tVar

fun NSData.toHexEncodedString(): String {
    val bytes = bytes!!.reinterpret<uint8_tVar>()
    val builder = StringBuilder(capacity = length.toInt())
    for (i in 0 until length.toInt()) {
        builder.append(NSString.create(format = "%02hhx", args = arrayOf(bytes[i])))
    }
    val str = NSString.create(string = builder.toString())
    return str.toString()
}