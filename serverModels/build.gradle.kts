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
                extra.libsAt(listOf(
                    "kotlin.serializationJson",
                    "kotlin.stdLib"
                )).forEach { implementation(it) }
            }
        }
        val androidMain by getting {
            dependencies {
                extra.libsAt(listOf(
                    "android.annotation"
                )).forEach { implementation(it) }
            }
        }
    }
}