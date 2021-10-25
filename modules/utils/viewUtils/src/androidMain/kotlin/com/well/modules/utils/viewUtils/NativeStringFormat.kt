package com.well.modules.utils.viewUtils

actual fun String.Companion.nativeFormat(format: String, vararg args: Any?): String =
    String.format(format, *args)