package com.well.utils.base

expect class UrlUtil {
    companion object {
        fun isValidUrl(url: String): Boolean
    }
}
