plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(project = project)
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                ":modules:features:chatList:chatListFeature",
                ":modules:utils:flowUtils",
                ":modules:utils:viewUtils",
                ":modules:models",
                ":modules:atomic",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "sqldelight.coroutinesExtensions",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}