plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    ios()
    jvm()
    android()
    sourceSets {
        val commonMain by getting {
            dependencies {
                extra.libsAt(listOf(
                    "kotlin.serializationJson",
//                    "klock",
                    "kotlin.stdLib"
                )).forEach { implementation(it) }
            }
        }
        val jvmMain by getting {
//            dependencies {
//                extra.libsAt(listOf(
//                    "android.annotation"
//                )).forEach { implementation(it) }
//            }
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