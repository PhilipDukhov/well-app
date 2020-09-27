plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-android")
}
group = "com.well.app"
version = "1.0-SNAPSHOT"

android {
    defaultConfig {
        applicationId = "com.well.app.androidApp"
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
apply(from = "$projectDir/dependencies.gradle")