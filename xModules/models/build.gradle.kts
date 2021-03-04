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
        all {
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            libDependencies(
                "kotlin.serializationJson",
                "ktor.client.serialization",
                "ktor.client.core",
                "kotlin.stdLib"
            )
        }
    }
}