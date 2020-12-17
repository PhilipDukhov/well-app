package com.well.utils

actual object Platform {
    actual val isDebug = kotlin.native.Platform.isDebugBinary
}