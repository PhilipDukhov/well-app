plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlin-android-extensions")
    id("kotlinx-serialization")
}

kotlin {
    android()
    ios()
    cocoapods {
        summary = "Shared"
        homepage = "-"
        license = "-"
        ios.deploymentTarget = extra.version("iosDeploymentTarget")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":serverModels"))
                extra.libsAt(listOf(
                    "kotlin.coroutines.core",
                    "kotlin.serializationJson",
                    "kotlin.stdLib",
                    "napier",
                    "oolong"
                )).forEach { implementation(it) }
            }
        }
        val androidMain by getting {
            dependencies {
                extra.libsAt(listOf(
                    "kotlin.coroutines.playServices",
                    "firebase.storage"
                )).forEach { implementation(it) }
            }
        }
        val iosMain by getting {
            dependencies {
                extra.libsAt(listOf(
                    "kotlin.serializationJson",
                    "napier",
                    "kotlin.stdLib",
                    "oolong"
                )).forEach { implementation(it) }
            }
        }
    }
}