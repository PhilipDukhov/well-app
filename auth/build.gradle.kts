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

        pod("Firebase/Core", version = "7.1.0", moduleName = "FirebaseCore")
        pod("GoogleSignIn", version = "5.0.2")
        pod("FBSDKLoginKit", version = "8.2.0")
        pod("FBSDKCoreKit", version = "8.2.0")
    }
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":serverModels",
                ":utils",
                "kotlin.serializationJson",
                "napier",
                "oolong",
                "ktor.client.cio",
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
        }
        val androidMain by getting {
            libDependencies(
                "facebookLogin",
                "google.playServicesAuth",
                "android.fragment",
                "kotlin.coroutines.playServices"
            )
        }
    }
}
