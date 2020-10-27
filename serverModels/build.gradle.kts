plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    ios()
    jvm()
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