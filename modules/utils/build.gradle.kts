plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
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
        val androidMain by getting {
            libDependencies(
                "android.appCompat",
                "android.dataStore",
                "android.activity",
            )
        }
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios",
            )
        }
    }
}