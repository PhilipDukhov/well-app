plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:models",
                ":modules:utils",
                "kotlin.coroutines.core",
                "kotlin.serializationJson",
                "kotlin.stdLib",
                "shared.napier",
            )
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "android.webrtc",
                )
            }
            val androidTest by getting {
                libDependencies(
                    "tests.junit",
                )
                dependencies {
                    implementation(kotlin("test-junit"))
                }
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}