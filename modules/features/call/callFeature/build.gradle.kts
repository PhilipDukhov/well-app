plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    android()
    iosWithSimulator(project = project)
    sourceSets {
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:models",
                ":modules:puerhBase",
                ":modules:utils:viewUtils",
                ":modules:utils:kotlinUtils",
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
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}