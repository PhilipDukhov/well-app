plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    iosWithSimulator(includeSimulator = true)
    jvm()
    sourceSets {
        optIns()
        if (withAndroid) {
            val androidMain by getting {
                kotlin.srcDir("src/jvmMain/kotlin")
            }
        }
    }
}
