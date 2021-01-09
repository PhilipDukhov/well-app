plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlinx-serialization")
}

kotlin {
    android()
    val frameworkName = project.name.capitalize()
    val onPhone = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
    if (onPhone) {
        iosArm64("ios") {
            binaries {
                framework(frameworkName) {
                    freeCompilerArgs += listOf("-Xobjc-generics")
                }
            }
        }
    } else {
        iosX64("ios")
    }
    cocoapods {
        this.frameworkName = frameworkName
        summary = frameworkName
        homepage = "-"
        license = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")

        pod("GoogleWebRTC", moduleName = "WebRTC")
    }
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("io.ktor.util.InternalAPI")
        }

        val commonMain by getting {
            libDependencies(
                ":serverModels",
                ":utils",
                "kotlin.serializationJson",
                "napier",
                "ktor.client.core",
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
        }
        val androidMain by getting {
            libDependencies(
                "webrtc",
                "android.activity",
                "ktor.client.engine.cio",
                "kotlin.coroutines.playServices"
            )
        }
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios"
            )
        }
    }
}
