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
                ":modules:puerhBase",
                ":modules:utils:viewUtils",
                "kotlin.datetime",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
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