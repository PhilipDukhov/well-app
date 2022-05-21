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
                ":modules:atomic",
                ":modules:models",
                ":modules:utils:viewUtils",
                ":modules:utils:ktorUtils",
                ":modules:utils:kotlinUtils",
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
                "kotlin.stdLib",
                "ktor.client.core",
                "ktor.client.logging",
                "ktor.utils",
                "shared.napier",
                "shared.okio",
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