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