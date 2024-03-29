plugins {
    kotlin("multiplatform")
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
        findByName("iosMain")?.run {
            libDependencies(
//                "kotlin.coroutines.core-strictly",
            )
        }
    }
}