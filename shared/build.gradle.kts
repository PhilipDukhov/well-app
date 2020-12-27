plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlinx-serialization")
}

kotlin {
    android()
    val onPhone = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
    if (onPhone) {
        iosArm64("ios")
    } else {
        iosX64("ios")
    }
    cocoapods {
        frameworkName = "Shared"
        summary = frameworkName
        homepage = "-"
        license = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")

//        pod("GoogleWebRTC", moduleName = "WebRTC", version = "1.1.31999")
    }
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":serverModels",
                ":utils",
                "kotlin.serializationJson",
                "napier",
                "oolong",
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