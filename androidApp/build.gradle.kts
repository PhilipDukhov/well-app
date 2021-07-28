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
        freeCompilerArgs += listOf(
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-Xskip-prerelease-check",
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
        val dotEnv = DotEnv(project)
        listOf("debug", "release").forEach { buildType ->
            getByName(buildType) {
                listOf(
                    "project_id" to dotEnv["GOOGLE_PROJECT_ID"],
                    "facebook_app_id" to dotEnv.facebookAppId,
                    "fb_login_protocol_scheme" to "fb${dotEnv.facebookAppId}",
                    "gcm_defaultSenderId" to dotEnv.googleAppId,
                    "google_app_id" to "1:${dotEnv.googleAppId}:android:23b05b40100225534c61a4",
                    "google_api_key" to dotEnv.googleApiKey,
                    "google_crash_reporting_api_key" to dotEnv.googleApiKey,
                    "default_web_client_id" to dotEnv.googleWebClientIdFull,
                    "google_web_client_id" to dotEnv.googleWebClientIdFull,
                ).forEach { pair ->
                    resValue("string", pair.first, pair.second)
                }
            }
        }
    }
}