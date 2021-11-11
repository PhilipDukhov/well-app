plugins {
    kotlin("multiplatform")
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
                ":modules:db:usersDb",
                ":modules:db:chatMessagesDb",
                ":modules:utils:dbUtils",
                ":modules:atomic",
                ":modules:utils:viewUtils",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "sqldelight.coroutinesExtensions",
                "sqldelight.runtime",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "sqldelight.androidDriver",
                    // TODO: issue not imported from :modules:utils
                    "android.activity",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "sqldelight.nativeDriver",
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}