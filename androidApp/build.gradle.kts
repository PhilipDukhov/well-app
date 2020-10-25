plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-android")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

android {
    defaultConfig {
        applicationId = "com.well.androidApp"
        multiDexEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }
    lintOptions {
        isAbortOnError = false
    }
}
apply(from = "$projectDir/dependencies.gradle")