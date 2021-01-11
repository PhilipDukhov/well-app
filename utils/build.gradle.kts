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
                ":serverModels",
                "kotlin.coroutines.core",
                "napier",
                "kotlin.stdLib"
            )
        }
        val androidMain by getting {
            libDependencies(
                "android.appCompat",
                "android.dataStore",
                "android.activity"
            )
        }
    }
}