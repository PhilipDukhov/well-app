package com.well.atomic

import kotlin.native.concurrent.freeze

actual fun <T> T.freeze(): T = freeze()

