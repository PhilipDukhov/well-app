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
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:puerhBase",
                ":modules:utils:kotlinUtils",
                ":modules:utils:viewUtils",
                "kotlin.stdLib",
                "kotlin.datetime",
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