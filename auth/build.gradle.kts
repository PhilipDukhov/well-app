plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
}

kotlin {
    android()
    // Revert to just ios() when gradle plugin can properly resolve it
    val onPhone = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
    if (onPhone) {
        iosArm64("ios")
    } else {
        iosX64("ios")
    }
    cocoapods {
        frameworkName = "Auth"
        summary = frameworkName
        homepage = "-"
        license = "-"
        ios.deploymentTarget = extra.version("iosDeploymentTarget")

        pod("Firebase/Core") {
            moduleName = "FirebaseCore"
            version = "~> 7.1.0"
        }
        pod("GoogleSignIn") {
            version = "~> 5.0.2"
        }
        pod("FBSDKLoginKit") {
            version = "~> 8.2.0"
        }
        pod("FBSDKCoreKit") {
            version = "~> 8.2.0"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                extra.libsAt(
                    listOf(
                        "kotlin.serializationJson",
                        "napier",
                        "oolong",
                        "ktor.client.cio",
                        "kotlin.stdLib"
                    )
                ).forEach { implementation(it) }
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                    version { strictly("1.3.9-native-mt-2") }
                }
            }
        }
        val androidMain by getting {
            dependencies {
                extra.libsAt(
                    listOf(
                        "facebookLogin",
                        "google.playServicesAuth",
                        "android.fragment",
                        "kotlin.coroutines.playServices"
                    )
                ).forEach { implementation(it) }
            }
        }
    }
}