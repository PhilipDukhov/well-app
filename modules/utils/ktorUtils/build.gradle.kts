plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "ktor.client.core",
                "ktor.client.serialization",
                "ktor.client.logging",
                "kotlin.stdLib",
            )
        }
        val androidMain by getting {
            libDependencies(
                "ktor.client.engine.cio",
            )
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
                "ktor.client.engine.ios"
            )
        }
    }
}