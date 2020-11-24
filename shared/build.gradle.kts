plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlinx-serialization")
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
        frameworkName = "Shared"
        summary = frameworkName
        homepage = "-"
        license = "-"
        ios.deploymentTarget = extra.version("iosDeploymentTarget")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                listOf(
                    ":serverModels"
                ).forEach { implementation(project(it)) }
                extra.libsAt(
                    listOf(
                        "kotlin.serializationJson",
                        "napier",
                        "oolong",
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
                        "kotlin.coroutines.playServices"
                    )
                ).forEach { implementation(it) }
            }
        }
        val iosMain by getting {
            dependencies {
                extra.libsAt(
                    listOf(
                        "kotlin.serializationJson",
                        "napier",
                        "kotlin.stdLib",
                        "oolong"
                    )
                ).forEach { implementation(it) }
            }
        }
    }
}