package com.well.utils

import com.well.utils.BuildConfig

actual object Platform {
    actual val isDebug: Boolean
        get() = BuildConfig.DEBUG
}