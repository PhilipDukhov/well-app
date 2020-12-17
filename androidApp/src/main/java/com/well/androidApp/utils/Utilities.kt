package com.well.androidApp.utils

import android.os.Build
import kotlin.system.measureNanoTime

object Utilities {
    inline fun printlnMeasure(message: String, block: () -> Unit) =
        println("$message ${measureNanoTime(block).toDouble() / 1_000_000_000 * 60}")

    fun isProbablyAnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                && Build.FINGERPRINT.endsWith(":user/release-keys")
                && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                && Build.MODEL.startsWith("sdk_gphone_")) // Android SDK emulator
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || "QC_Reference_Phone" == Build.BOARD  //bluestacks
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build") //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
    }
}