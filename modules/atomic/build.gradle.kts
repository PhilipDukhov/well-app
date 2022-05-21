plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "kotlin.coroutines.core",
                "kotlin.stdLib",
            )
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}