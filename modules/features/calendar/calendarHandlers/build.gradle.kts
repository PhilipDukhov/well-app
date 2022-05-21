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
                ":modules:atomic",
                ":modules:features:calendar:calendarFeature",
                ":modules:utils:flowUtils",
                ":modules:utils:viewUtils",
                ":modules:models",
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
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}