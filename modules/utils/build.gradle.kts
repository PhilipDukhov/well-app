plugins {
    kotlin("multiplatform")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    ios()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:napier",
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "android.appCompat",
                    "android.dataStore",
                    "android.activity",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios",
            )
        }
    }
}