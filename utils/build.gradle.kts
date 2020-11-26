plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    // Revert to just ios() when gradle plugin can properly resolve it
    ios()
//    val onPhone = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
//    if (onPhone) {
//        iosArm64("ios")
//    } else {
//        iosX64("ios")
//    }
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "kotlin.stdLib"
            )
        }
    }
}