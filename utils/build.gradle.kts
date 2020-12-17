plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    ios()
    sourceSets {
        val androidMain by getting {
            libDependencies(
                "android.appCompat",
                "android.activity"
            )
        }
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
        }
    }
}