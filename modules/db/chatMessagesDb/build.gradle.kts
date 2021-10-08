plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
    if (withAndroid) {
        id("com.android.library")
    }
}

sqldelight {
    database("ChatMessagesDatabase") {
        packageName = "com.well.modules.db.chatMessages"
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator()
    jvm()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:flowHelper",
                ":modules:db:helperDb",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "sqldelight.coroutinesExtensions",
                "sqldelight.runtime",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "sqldelight.androidDriver",
                    "android.activity",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "sqldelight.nativeDriver",
                "kotlin.coroutines.core-strictly",
            )
        }
        val jvmMain by getting {
            libDependencies(
                "sqldelight.sqliteDriver",
            )
        }

    }
}