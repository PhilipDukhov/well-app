package com.well.modules.utils.viewUtils

import kotlin.text.toChars as toCharsNative

actual fun Char.Companion.toChars(codePoint: Int): CharArray = toCharsNative(codePoint)