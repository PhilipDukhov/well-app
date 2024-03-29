plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
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
        val androidMain by getting {
            libDependencies(
                "android.coil",
                "android.dataStore",
                "android.material",
                "android.activity",
                "android.browser",
                "firebase.messaging",
            )
            dependencies {
                implementation(dependencies.platform(libAt("firebase.bom")))
            }
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}