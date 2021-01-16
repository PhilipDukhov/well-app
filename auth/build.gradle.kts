plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
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
        frameworkName = "Auth"
        summary = frameworkName
        homepage = "-"
        license = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")

//        pod("GoogleSignIn", version = "5.0.2")
//        pod("FBSDKLoginKit", version = "8.2.0")
//        pod("FBSDKCoreKit", version = "8.2.0")
    }
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":serverModels",
                ":utils",
                "kotlin.serializationJson",
                "ktor.client.core",
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
        }
        val androidMain by getting {
            libDependencies(
                "facebookLogin",
                "google.playServicesAuth",
                "android.fragment",
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
