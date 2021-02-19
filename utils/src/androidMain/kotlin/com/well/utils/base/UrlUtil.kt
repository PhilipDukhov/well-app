package com.well.utils.base

import android.webkit.URLUtil

actual class UrlUtil {
    actual companion object {
        actual fun isValidUrl(url: String) =
            URLUtil.isValidUrl(url)
    }
}