package com.well.utils.platform

import com.well.utils.BuildConfig

actual val Platform.Companion.isDebug: Boolean
    get() = !prodTesting && BuildConfig.DEBUG

actual val Platform.Companion.nativeScale: Float
    get() = 1F


actual val Platform.Companion.current: Platform.Platform
    get() = Platform.Platform.Android