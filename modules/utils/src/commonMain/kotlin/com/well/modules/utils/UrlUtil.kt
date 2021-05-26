package com.well.modules.utils

expect class UrlUtil {
    companion object {
        fun isValidUrl(url: String): Boolean
    }
}
