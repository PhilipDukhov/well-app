import io.github.cdimascio.dotenv.dotenv
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

dependencies {
    implementation(platform(libAt("firebase.bom")))
}

libDependencies(
    "android.material",
    "android.appCompat",
    "android.constraint",
    "android.coreCtx",
    "android.activity",
    "android.fragment",

    "android.compose.*",

    "firebase.analytics",

    "kotlin.coroutines.playServices",
    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "kotlin.reflect",
    "webrtc",

    //tmp
    "ktor.client.core",

    ":atomic",
    ":serverModels",
    ":utils",
    ":sharedMobile"
)

android {
    defaultConfig {
        applicationId = "com.well.androidApp"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
    lintOptions {
        isAbortOnError = false
    }
    kotlinOptions {
        useIR = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = project.version("compose")
    }
    signingConfigs {
        val localProps = gradleLocalProperties(rootDir)
        val storeFilePath: String by localProps
        val storePassword: String by localProps
        val keyAlias: String by localProps
        create("release") {
            this.storeFile = file(storeFilePath)
            this.storePassword = storePassword
            this.keyAlias = keyAlias
            this.keyPassword = storePassword
        }
    }
    buildTypes {
        getByName("release") {
//            isMinifyEnabled = true
//            isShrinkResources = true
//            -dontnote kotlinx.serialization.SerializationKt
//            -keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.
//            -keep public class * extends java.lang.Exception  # Optional: Keep custom exceptions.
            ndk {
                debugSymbolLevel = "FULL"
            }
            resValue("string", "app_name", "WELL")
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "WELL_D")
        }
        val dotenv = dotenv {
            directory = "${projectDir}/../iosApp/Well/Supporting files/"
            filename = "Shared.xcconfig"
        }

        listOf("debug", "release").forEach {
            val facebookAppId = dotenv["SHARED_FACEBOOK_APP_ID"]
            val googleWebClientId = dotenv["ANDROID_GOOGLE_CLIENT_ID"]
            val googleWebClientIdFull = "$googleWebClientId.apps.googleusercontent.com"
            val googleProjectId = dotenv["GOOGLE_PROJECT_ID"]
            val googleAppId = dotenv["GOOGLE_APP_ID"]
            val apiKey = "AIzaSyAPi16R8G5T88zmqmCPwgK6NHv3zXU6BFY"

            getByName(it) {
                listOf(
                    "facebook_app_id" to facebookAppId,
                    "fb_login_protocol_scheme" to "fb$facebookAppId",
                    "gcm_defaultSenderId" to googleAppId,
                    "google_app_id" to "1:${googleAppId}:android:23b05b40100225534c61a4",
                    "google_api_key" to apiKey,
                    "google_crash_reporting_api_key" to apiKey,
                    "project_id" to googleProjectId,
                    "default_web_client_id" to googleWebClientIdFull,
                    "google_web_client_id" to googleWebClientIdFull
                ).forEach { pair ->
                    resValue("string", pair.first, pair.second)
                }
            }
        }
    }
}
