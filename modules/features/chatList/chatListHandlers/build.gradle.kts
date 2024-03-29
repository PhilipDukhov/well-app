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
                ":modules:features:chatList:chatListFeature",
                ":modules:utils:flowUtils",
                ":modules:utils:viewUtils",
                ":modules:models",
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "sqldelight.coroutinesExtensions",
            )
        }
        val androidMain by getting {
            libDependencies(
            )
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}