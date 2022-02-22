package com.well.modules.atomic

import kotlin.native.concurrent.freeze

actual fun <T> T.freeze(): T = freeze()

