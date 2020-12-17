plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

kotlin {
    android()
    ios()
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
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios"
            )
        }
    }
}
