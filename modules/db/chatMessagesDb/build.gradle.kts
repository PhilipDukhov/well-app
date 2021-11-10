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
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        optIns(optIns = setOf(OptIn.Coroutines))
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:utils:flowUtils",
                ":modules:db:helperDb",
                "shared.napier",
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