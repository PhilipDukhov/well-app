plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:puerhBase",
                ":modules:utils:kotlinUtils",
                ":modules:utils:viewUtils",
                "kotlin.stdLib",
                "kotlin.datetime",
                "shared.napier",
            )
        }
        val androidMain by getting {
            libDependencies(
            )
        }
        val iosMain by getting {
            libDependencies(
//                "kotlin.coroutines.core-strictly",
            )
        }
    }
}