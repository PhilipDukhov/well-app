plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
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