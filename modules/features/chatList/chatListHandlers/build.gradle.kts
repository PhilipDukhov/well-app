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
                ":modules:features:chatList:chatListFeature",
                ":modules:db:chatMessagesDb",
                ":modules:db:usersDb",
                ":modules:flowHelper",
                ":modules:viewHelpers",
                ":modules:models",
                ":modules:utils",
                ":modules:atomic",
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