plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(project = project)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:utils:kotlinUtils",
                "kotlin.coroutines.core",
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
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}