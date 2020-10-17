package com.well.androidApp

import kotlin.system.measureTimeMillis

object Utilities {
    inline fun printlnMeasure(message: String, block: () -> Unit) =
        println("$message ${measureTimeMillis(block).toDouble() / 1000}")
}
