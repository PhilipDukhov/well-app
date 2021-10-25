plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(includeSimulator = true)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:puerhBase",
                "kotlin.stdLib",
                "shared.napier",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                )
            }
        }
        val iosMain by getting {
            libDependencies(
//                "kotlin.coroutines.core-strictly",
            )
        }
    }
}