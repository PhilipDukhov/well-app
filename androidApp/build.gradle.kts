import io.github.cdimascio.dotenv.dotenv
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

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
    "android.coreCtx",
    "android.activity",

    "android.compose.*",

    "firebase.analytics",

    "kotlin.coroutines.playServices",
    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "kotlin.reflect",
    "webrtc",

    //tmp
    "ktor.client.core",

    ":modules:atomic",
    ":modules:models",
    ":modules:utils",
    ":sharedMobile",
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
    kotlinOptions {
        useIR = true

        freeCompilerArgs += listOf(
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-Xskip-prerelease-check",
            "-Xallow-jvm-ir-dependencies",
        )
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
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
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
        val facebookAppId = dotenv["SHARED_FACEBOOK_APP_ID"]
        val googleWebClientId = dotenv["ANDROID_GOOGLE_CLIENT_ID"]
        val googleWebClientIdFull = "$googleWebClientId.apps.googleusercontent.com"
        val googleAppId = dotenv["GOOGLE_APP_ID"]
        val apiKey = "GOOGLE_API_KEY"

        listOf("debug", "release").forEach { buildType ->
            getByName(buildType) {
                (
                    listOf(
                        "project_id" to "GOOGLE_PROJECT_ID",
                        "apple_server_client_id" to "APPLE_SEVER_CLIENT_ID",
                        "apple_auth_redirect_url" to "APPLE_AUTH_REDIRECT_URL",
                    ).map {
                        it.first to dotenv[it.second]
                    } +
                        listOf(
                            "facebook_app_id" to facebookAppId,
                            "fb_login_protocol_scheme" to "fb$facebookAppId",
                            "gcm_defaultSenderId" to googleAppId,
                            "google_app_id" to "1:${googleAppId}:android:23b05b40100225534c61a4",
                            "google_api_key" to apiKey,
                            "google_crash_reporting_api_key" to apiKey,
                            "default_web_client_id" to googleWebClientIdFull,
                            "google_web_client_id" to googleWebClientIdFull,
                        )
                    ).forEach { pair ->
                        resValue("string", pair.first, pair.second)
                    }
            }
        }
    }
}