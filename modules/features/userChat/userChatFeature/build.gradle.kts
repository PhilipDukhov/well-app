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
                ":modules:utils:viewUtils",
                ":modules:puerhBase",
                "kotlin.datetime",
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
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}