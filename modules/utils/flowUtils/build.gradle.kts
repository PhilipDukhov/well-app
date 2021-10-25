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
    iosWithSimulator()
    jvm()
    sourceSets {
        optIns(optIns = setOf(OptIn.Coroutines))
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                "kotlin.coroutines.core",
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