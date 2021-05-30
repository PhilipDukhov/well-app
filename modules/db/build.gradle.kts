plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
    if (withAndroid) {
        id("com.android.library")
    }
}

sqldelight {
    database("Database") {
        packageName = "com.well.modules.db"
    }
}

kotlin {
    androidWithAndroid()
    ios()
    sourceSets {
        usePredefinedExperimentalAnnotations()
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:dbHelper",
                ":modules:utils",
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
            )
        }
    }
}