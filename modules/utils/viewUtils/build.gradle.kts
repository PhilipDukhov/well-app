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
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                ":modules:models",
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
                "kotlin.stdLib",
                "shared.napier",
                "sqldelight.coroutinesExtensions",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "android.coil",
                    "android.dataStore",
                    "android.material",
                    "android.activity",
                    "android.browser",
                )
            }
            val jvmMain by getting {
                kotlin.srcDir("src/androidMain/kotlin")
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}