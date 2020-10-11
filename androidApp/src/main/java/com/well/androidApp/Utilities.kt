package com.well.androidApp

import kotlin.system.measureTimeMillis

inline fun printlnMeasure(message: String, block: () -> Unit) =
    println("$message ${measureTimeMillis(block).toDouble() / 1000}")