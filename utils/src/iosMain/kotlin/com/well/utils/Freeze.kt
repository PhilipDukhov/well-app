package com.well.utils

import kotlin.native.concurrent.freeze

actual fun <T> T.freeze(): T = freeze()