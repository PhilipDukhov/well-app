plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:features:call:callFeature",
                ":modules:atomic",
                ":modules:models",
                ":modules:utils:viewUtils",
                ":modules:utils:kotlinUtils",
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
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