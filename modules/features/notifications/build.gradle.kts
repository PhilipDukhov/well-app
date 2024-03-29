plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                ":modules:utils:viewUtils",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
            )
        }
        val androidMain by getting {
            libDependencies(
                "android.appCompat",
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