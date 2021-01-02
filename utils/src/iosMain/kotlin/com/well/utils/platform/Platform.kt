package com.well.utils.platform

actual val Platform.Companion.isDebug: Boolean
    get() = kotlin.native.Platform.isDebugBinary