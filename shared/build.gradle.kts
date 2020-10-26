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
        homepage = ""
        ios.deploymentTarget = extra.version("iosDeploymentTarget")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":serverModels"))
                listOf(
                    "kotlin.serializationJson",
                    "kotlin.stdLib",
                    "napier",
                    "oolong"
                ).forEach {
                    implementation(extra.libAt(it))
                }
            }
        }
        val androidMain by getting {
            dependencies {
                listOf(
                    "kotlin.coroutinesPlayServices",
                    "firebase.storage"
                ).forEach {
                    implementation(extra.libAt(it))
                }
            }
        }
        val iosMain by getting {
            dependencies {
                listOf(
                    "kotlin.serializationJson",
                    "napier",
                    "kotlin.stdLib",
                    "oolong"
                ).forEach {
                    implementation(extra.libAt(it))
                }
            }
        }
    }
}
android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}