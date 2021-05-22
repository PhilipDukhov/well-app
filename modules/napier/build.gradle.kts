plugins {
    kotlin("multiplatform")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    ios()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
            )
        }
    }
}