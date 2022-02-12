plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
}

dependencies {
    implementation(platform(libAt("firebase.bom")))
    coreLibraryDesugaring()
}

libDependencies(
    "android.appCompat",
    "android.compose.*",
    "android.dataStore",
    "shared.napier",

    "kotlin.coroutines.core",

    ":sharedMobileTest",
    ":modules:androidUi",
    ":modules:utils:viewUtils",
    ":modules:features:more:moreFeature",
    ":modules:features:welcome",
    ":modules:features:myProfile:myProfileFeature",
    ":modules:features:call:callFeature",
    ":modules:features:login:loginFeature",
    ":modules:features:chatList:chatListFeature",
    ":modules:features:experts:expertsFeature",
    ":modules:features:userChat:userChatFeature",
    ":modules:features:calendar:calendarFeature",
)

android {
    defaultConfig {
        applicationId = "com.well.androidApp.test"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        optIns(OptIn.Compose)
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = project.version("compose")
    }
}