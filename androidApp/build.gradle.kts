plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("kotlin-android")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}
group = "com.well"
version = "1.0-SNAPSHOT"

android {
    defaultConfig {
        applicationId = "com.well.androidApp"
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    lintOptions {
        isAbortOnError = false
    }
}
apply(from = "$projectDir/dependencies.gradle")