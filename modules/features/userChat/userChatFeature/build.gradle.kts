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
                ":modules:models",
                ":modules:utils:viewUtils",
                ":modules:puerhBase",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "sqldelight.coroutinesExtensions",
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