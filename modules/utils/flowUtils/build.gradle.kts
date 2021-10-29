plugins {
    kotlin("multiplatform")
    if (withAndroid) {
        id("com.android.library")
    } else {
        `java-library`
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(includeSimulator = true, project = project)
    jvm()
    sourceSets {
        optIns(optIns = setOf(OptIn.Coroutines))
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "ktor.client.engine.cio",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}