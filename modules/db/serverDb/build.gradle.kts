plugins {
    kotlin("multiplatform")
    id("com.squareup.sqldelight")
}

sqldelight {
    database("Database") {
        packageName = "com.well.modules.db.server"
        dialect = "mysql"
    }
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":modules:models",
                ":modules:utils:dbUtils",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                "sqldelight.coroutinesExtensions",
                "kotlin.datetime",
            )
        }
    }
}