plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    iosWithSimulator(project = project)
    android()
    jvm()
    sourceSets {
        optIns(OptIn.Serialization)
        val commonMain by getting {
            libDependencies(
                ":modules:utils:dbUtils",
                ":modules:utils:kotlinUtils",
                "kotlin.datetime",
                "kotlin.serializationJson",
                "sqldelight.runtime",
                "kotlin.stdLib",
            )
        }
        val jvmMain by getting {

        }
        val androidMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
        }
    }
}

apply(from = "${rootDir}/androidEnableDesugaring.gradle.kts")
