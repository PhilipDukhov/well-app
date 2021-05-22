plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    } else {
        `java-library`
    }
}

kotlin {
    ios()
    androidWithAndroid()
    jvm()
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
        }
        val commonMain by getting {
            libDependencies(
                "kotlin.serializationJson",
                "ktor.client.core",
                "ktor.client.serialization",
                "ktor.client.logging",
                "kotlin.stdLib",
            )
        }
        val jvmMain by getting {

        }
        if (withAndroid) {
            val androidMain by getting {
                dependsOn(jvmMain)
            }
        }
    }
}