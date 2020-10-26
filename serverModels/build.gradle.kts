plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("plugin.serialization")
    id("com.android.library")
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
                implementation(kotlin("stdlib-common"))
                listOf(
                    "kotlin.serializationJson",
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