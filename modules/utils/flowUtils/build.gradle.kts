plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "shared.napier",
            )
        }
        val androidMain by getting {
            libDependencies(
                "ktor.client.engine.cio",
            )
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}