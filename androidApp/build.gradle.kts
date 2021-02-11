import io.github.cdimascio.dotenv.dotenv
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

libDependencies(
    "android.material",
    "android.appCompat",
    "android.constraint",
    "android.coreCtx",
    "android.activity",
    "android.fragment",

    "android.compose.*",

    "kotlin.coroutines.playServices",
    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "kotlin.reflect",
    "webrtc",

    //tmp
    "ktor.client.core",

//    ":auth",
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
        disable("PackageName")
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
            isMinifyEnabled = true
            isShrinkResources = true
            ndk {
                debugSymbolLevel = "FULL"
            }
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
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
