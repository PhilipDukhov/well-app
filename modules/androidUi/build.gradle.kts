plugins {
    id("com.android.library")
    kotlin("android")
}

libDependencies(
    "android.material",
    "android.appCompat",
    "android.activity",
    "android.compose.*",
    "android.webrtc",

    "ktor.utils",
    "shared.napier",
    "kotlin.datetime",

    "kotlin.coroutines.playServices",
    "kotlin.serializationJson",
    "kotlin.coroutines.core",
    "kotlin.reflect",

    ":modules:androidWebrtc",
    ":modules:atomic",
    ":modules:annotations",
    ":modules:db:mobileDb",
    ":modules:db:chatMessagesDb",
    ":modules:db:usersDb",
    ":modules:models",
    ":modules:utils",
    ":modules:flowHelper",
    ":modules:networking",
    ":modules:viewHelpers",
    ":modules:features:call:callFeature",
    ":modules:features:login",
    ":modules:features:more",
    ":modules:features:welcome",
    ":modules:features:myProfile",
    ":modules:features:chatList:chatListFeature",
    ":modules:features:experts:expertsFeature",
    ":modules:features:userChat:userChatFeature",
    ":sharedMobile",
)

android {
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        val optIns = listOf(
            "androidx.compose.ui.ExperimentalComposeUiApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "com.google.accompanist.pager.ExperimentalPagerApi",
            "androidx.compose.material.ExperimentalMaterialApi",
            "androidx.compose.animation.ExperimentalAnimationApi",
            "coil.annotation.ExperimentalCoilApi",
        )
        freeCompilerArgs += optIns.map { "-Xopt-in=$it" } + listOf(
            "-Xskip-prerelease-check",
        )
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = project.version("compose")
    }
}