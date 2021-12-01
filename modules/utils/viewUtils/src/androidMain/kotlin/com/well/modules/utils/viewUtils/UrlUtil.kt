package com.well.modules.utils.viewUtils

import android.webkit.URLUtil

actual object UrlUtil {
    actual fun isValidUrl(url: String) =
        URLUtil.isValidUrl(url)
}