plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:db:usersDb",
                ":modules:db:chatMessagesDb",
                ":modules:db:meetingsDb",
                ":modules:utils:dbUtils",
                ":modules:atomic",
                ":modules:utils:viewUtils",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "sqldelight.coroutinesExtensions",
                "sqldelight.runtime",
                "shared.napier",
            )
        }
        val androidMain by getting {
            libDependencies(
                "sqldelight.androidDriver",
                // TODO: issue not imported from :modules:utils
                "android.activity",
            )
        }
        findByName("iosMain")?.run {
            libDependencies(
                "sqldelight.nativeDriver",
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}