package com.well.utils.platform

import com.well.utils.BuildConfig

actual val Platform.Companion.isDebug: Boolean
    get() = BuildConfig.DEBUG
