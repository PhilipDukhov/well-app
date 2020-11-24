package com.well.auth

import platform.UIKit.UIApplication

actual data class Context(
    val application: UIApplication,
    val launchOptions: Map<Any?, Any>?,
)