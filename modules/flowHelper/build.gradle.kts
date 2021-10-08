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
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
            )
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}