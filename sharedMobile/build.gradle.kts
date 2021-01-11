plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlinx-serialization")
}

kotlin {
    android()
    val frameworkName = project.name.capitalize()
    val iosTargets = listOf(iosX64(), iosArm64())
    val iosTargetNames = iosTargets.map { it.name }
    configure(iosTargets) {
        binaries {
            framework(frameworkName) {
                freeCompilerArgs += listOf("-Xobjc-generics")
            }
        }
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
        usePredefinedExperimentalAnnotations()

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
                "android.compose.accompanist.coil",
                "ktor.client.engine.cio",
                "kotlin.coroutines.playServices"
            )
        }
        iosMainsBuild(iosTargetNames) {
            libDependencies(
                "ktor.client.engine.ios"
            )
        }
    }
}
