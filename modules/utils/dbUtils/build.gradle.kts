plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "sqldelight.runtime",
                "kotlin.datetime",
                "kotlin.serializationJson",
            )
        }
    }
}