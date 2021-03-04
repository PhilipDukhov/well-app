package com.well.utils

actual fun String.Companion.nativeFormat(format: String, vararg args: Any?): String =
    String.format(format, *args)
