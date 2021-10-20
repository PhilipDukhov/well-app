package com.well.modules.models.date.dateTime

actual class TestClass actual constructor() {
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

fun TestClass.jvmMethod() = platform + "123"