plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
                "kotlin.stdLib",
                "ktor.client.core",
                "ktor.utils",
                "shared.napier",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "android.appCompat",
                    "android.dataStore",
                    "android.activity",
                    "android.coil",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios",
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
