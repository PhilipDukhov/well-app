package com.well.modules.utils

import android.webkit.URLUtil

actual class UrlUtil {
    actual companion object {
        actual fun isValidUrl(url: String) =
            URLUtil.isValidUrl(url)
    }
}