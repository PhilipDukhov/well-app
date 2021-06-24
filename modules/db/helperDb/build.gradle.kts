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
    ios()
    jvm()
    sourceSets {
        val commonMain by getting {
            libDependencies(
                "sqldelight.runtime",
                "kotlin.serializationJson",
            )
        }
    }
}