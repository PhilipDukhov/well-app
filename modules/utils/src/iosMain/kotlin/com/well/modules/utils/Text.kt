package com.well.modules.utils

import kotlin.text.toChars as toCharsNative

actual fun Char.Companion.toChars(codePoint: Int): CharArray = toCharsNative(codePoint)