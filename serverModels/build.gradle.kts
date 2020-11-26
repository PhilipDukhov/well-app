plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    ios()
    android()
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "kotlin.serializationJson",
                "kotlin.stdLib"
            )
        }

        val androidMain by getting {
            libDependencies(
                "android.annotation"
            )
        }
    }
}