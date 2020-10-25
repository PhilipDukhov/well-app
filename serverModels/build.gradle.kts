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
        summary = "Server models module"
        homepage = ""
        ios.deploymentTarget = extra.version("iosDeploymentTarget")
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                listOf(
                    "kotlin.serialization",
                    "kotlin.stdLib"
                ).forEach {
                    implementation(extra.libAt(it))
                }
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(extra.libAt("jetpack.annotation"))
            }
        }
    }
}
android {
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}