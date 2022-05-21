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
                ":modules:atomic",
                ":modules:models",
                ":modules:puerhBase",
                ":modules:utils:viewUtils",
                ":modules:utils:kotlinUtils",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
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