package com.well.modules.utils.viewUtils

import android.webkit.URLUtil

actual class UrlUtil {
    actual companion object {
        actual fun isValidUrl(url: String) =
            URLUtil.isValidUrl(url)
    }
}