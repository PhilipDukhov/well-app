plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    } else {
        `java-library`
    }
}

kotlin {
    iosWithSimulator(project = project)
    androidWithAndroid()
    jvm()
    sourceSets {
        optIns(OptIn.Serialization)
        val commonMain by getting {
            libDependencies(
                ":modules:utils:dbUtils",
                "kotlin.datetime",
                "kotlin.serializationJson",
                "sqldelight.runtime",
                "kotlin.stdLib",
            )
        }
        val jvmMain by getting {

        }
        if (withAndroid) {
            val androidMain by getting {
                kotlin.srcDir("src/jvmMain/kotlin")
            }
        }
    }
}

if (withAndroid) {
    apply(from = "${rootDir}/androidEnableDesugaring.gradle.kts")
}