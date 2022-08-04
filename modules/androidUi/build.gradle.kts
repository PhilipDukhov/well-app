plugins {
    id("com.android.library")
    kotlin("android")
}

dependencies {
    coreLibraryDesugaring()
    libsAt("android.composeDebug").forEach(::debugImplementation)
}

libDependencies(
    "android.material",
    "android.appCompat",
    "android.activity",
    "android.compose.*",
    "android.webrtc",
    "android.dataStore",
    "android.dynamicanimation",

    "ktor.utils",
    "shared.napier",
    "kotlin.datetime",

    "kotlin.coroutines.playServices",
    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "kotlin.reflect",

    ":modules:androidWebrtc",
    ":modules:annotations",
    ":modules:models",
    ":modules:utils:viewUtils",
    ":modules:utils:kotlinUtils",
    ":modules:features:_topLevel:topLevelFeature",
    ":modules:features:more:moreFeature",
    ":modules:features:welcome",
    ":modules:features:myProfile:myProfileFeature",
    ":modules:features:call:callFeature",
    ":modules:features:login:loginFeature",
    ":modules:features:chatList:chatListFeature",
    ":modules:features:experts:expertsFeature",
    ":modules:features:userChat:userChatFeature",
    ":modules:features:calendar:calendarFeature",
    ":modules:features:updateRequest",
)

android {
    defaultConfig {
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