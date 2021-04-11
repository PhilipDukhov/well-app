package com.well.modules.utils.base

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class UrlUtil {
    actual companion object {
        actual fun isValidUrl(url: String) =
            UIApplication.sharedApplication.canOpenURL(NSURL(string = url))
    }
}