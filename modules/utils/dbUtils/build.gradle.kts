plugins {
    kotlin("multiplatform")
    if (withAndroid) {
        id("com.android.library")
    } else {
        `java-library`
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(project = project)
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "sqldelight.runtime",
                "kotlin.datetime",
                "kotlin.serializationJson",
            )
        }
    }
}