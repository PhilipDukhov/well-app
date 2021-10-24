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
    iosWithSimulator()
    androidWithAndroid()
    jvm()
    sourceSets {
        usePredefinedExperimentalAnnotations("kotlinx.serialization.ExperimentalSerializationApi")
        val commonMain by getting {
            libDependencies(
                "kotlin.datetime",
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
                kotlin.srcDir("src/jvmMain/kotlin")
            }
        }
    }
}

if (withAndroid) {
    android {
        compileOptions {
            isCoreLibraryDesugaringEnabled = true
        }
    }

    dependencies {
        coreLibraryDesugaring()
    }
}