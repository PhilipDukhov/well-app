import io.github.cdimascio.dotenv.dotenv

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("com.google.firebase.crashlytics")
    id("com.google.gms.google-services")
}

libDependencies(
    ":shared",
    ":auth",
    ":serverModels",

    "android.material",
    "android.appCompat",
    "android.constraint",
    "android.navigationFragment",
    "android.navigationUi",
    "android.coreCtx",
    "android.activity",
    "android.fragment",
    "firebase.analytics",
    "firebase.crashlytics",
    "kotlin.coroutines.playServices",
    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "glide.glide",
    "glide.compiler",
    "oolong",
    "napier",
    "viewbindingpropertydelegate",
    "webrtc"
)

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