plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
    if (withAndroid) {
        id("com.android.library")
    }
}

sqldelight {
    database("UsersDatabase") {
        packageName = "com.well.modules.db.users"
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(project = project)
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:utils:dbUtils",
                ":modules:utils:flowUtils",
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
                    // TODO: issue not imported from :modules:utils
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
    }
}