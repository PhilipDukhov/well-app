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
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}