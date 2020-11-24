import io.github.cdimascio.dotenv.dotenv

plugins {
    id("com.android.application")
    kotlin("android")
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
    buildTypes {
        val dotenv = dotenv {
            directory = "${projectDir}/../iosApp/Well/Supporting files/"
            filename = "Shared.xcconfig"
        }
        val facebookAppId = dotenv["SHARED_FACEBOOK_APP_ID"]
        val googleWebClientId = dotenv["ANDROID_GOOGLE_CLIENT_ID"]

        listOf("debug", "release").forEach {
            getByName(it) {
                listOf(
                    "facebook_app_id" to facebookAppId,
                    "fb_login_protocol_scheme" to "fb$facebookAppId",
                    "google_web_client_id" to "$googleWebClientId.apps.googleusercontent.com"
                ).forEach { pair ->
                    resValue("string", pair.first, pair.second)
                }
            }
        }
    }
}
apply(from = "$projectDir/dependencies.gradle")

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core") {
        version { strictly("1.3.9-native-mt-2") }
    }
}