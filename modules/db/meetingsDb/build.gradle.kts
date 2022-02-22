plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
    id("com.android.library")
}

sqldelight {
    database("MeetingsDatabase") {
        packageName = "com.well.modules.db.meetings"
    }
}

kotlin {
    android()
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:utils:flowUtils",
                ":modules:utils:dbUtils",
                "shared.napier",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "kotlin.datetime",
                "sqldelight.coroutinesExtensions",
                "sqldelight.runtime",
            )
        }
        val androidMain by getting {
            libDependencies(
                "sqldelight.androidDriver",
                "android.activity",
            )
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