plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:utils:viewUtils",
                ":modules:puerhBase",
                "kotlin.datetime",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                ":modules:utils:flowUtils",
                "kotlin.datetime",
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