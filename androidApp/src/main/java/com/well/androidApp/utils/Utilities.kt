package com.well.androidApp.utils

import kotlin.system.measureNanoTime

object Utilities {
    inline fun printlnMeasure(message: String, block: () -> Unit) =
        println("$message ${measureNanoTime(block).toDouble() / 1000000000 * 60}")
}