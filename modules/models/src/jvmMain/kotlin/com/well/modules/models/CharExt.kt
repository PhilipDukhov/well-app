package com.well.modules.models

import kotlin.text.isUpperCase as nativeIsUpperCase
import kotlin.text.isLowerCase as nativeIsLowerCase

actual fun Char.isUpperCase() = nativeIsUpperCase()
actual fun Char.isLowerCase() = nativeIsLowerCase()