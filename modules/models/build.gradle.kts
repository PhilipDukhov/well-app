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
    iosWithSimulator(includeSimulator = true)
    androidWithAndroid()
    jvm()
    sourceSets {
        optIns("kotlinx.serialization.ExperimentalSerializationApi")
        val commonMain by getting {
            libDependencies(
                "kotlin.datetime",
                "kotlin.serializationJson",
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