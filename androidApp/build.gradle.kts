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
        applicationId = "com.well.philip.android.app.test.gone.well"
        versionCode = 1
        versionName = "1.0"
    }
}
apply(from = "$projectDir/dependencies.gradle")