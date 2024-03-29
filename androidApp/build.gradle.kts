import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(platform(libAt("firebase.bom")))
    coreLibraryDesugaring()
}

libDependencies(
    "android.appCompat",
    "android.webrtc",
    "android.compose.core.activity",

    "firebase.analytics",
    "firebase.messaging",
    "shared.napier",

    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "kotlin.reflect",

    ":modules:androidUi",
    ":modules:androidWebrtc",
    ":modules:utils:viewUtils",
    ":modules:utils:flowUtils",
    ":modules:features:_topLevel:topLevelFeature",
    ":modules:features:_topLevel:topLevelHandlers",
    ":modules:features:login:loginFeature",
    ":modules:features:notifications",
    ":modules:features:login:loginHandlers",
    ":modules:features:call:callFeature",
)

android {
    defaultConfig {
        applicationId = "com.well.androidApp"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = project.version("compose")
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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
                    "facebook_client_token" to dotEnv["SHARED_FACEBOOK_CLIENT_TOKEN"],
                    "fb_login_protocol_scheme" to "fb${dotEnv.facebookAppId}",
                    "gcm_defaultSenderId" to dotEnv.gcmSenderId,
                    "google_app_id" to "1:${dotEnv.gcmSenderId}:android:23b05b40100225534c61a4",
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