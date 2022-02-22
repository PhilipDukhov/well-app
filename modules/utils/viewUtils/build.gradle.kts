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
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                ":modules:models",
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
                "kotlin.datetime",
                "kotlin.stdLib",
                "shared.napier",
                "shared.okio",
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
                dependencies {
                    api(libAt("firebase.messaging"))
                }
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}

dependencies {
    implementation(platform(libAt("firebase.bom")))
}