package com.well.modules.utils.base

import kotlinx.cinterop.convert
import platform.Foundation.NSDataDetector
import platform.Foundation.NSMakeRange
import platform.Foundation.NSTextCheckingTypeLink
import platform.Foundation.firstMatchInString
import platform.darwin.NSUInteger

actual class UrlUtil {
    actual companion object {
        actual fun isValidUrl(url: String) : Boolean {
            val detector = NSDataDetector(types = NSTextCheckingTypeLink, error = null)
            val range = NSMakeRange(0,
                url.length.toNSUInteger()
            )
            return detector.firstMatchInString(url, options = 0, range = range)?.range == range
        }
    }
}

inline fun Int.toNSUInteger(): NSUInteger {
    return convert()
}