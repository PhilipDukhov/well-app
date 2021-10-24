import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.plugin.PLUGIN_CLASSPATH_CONFIGURATION_NAME

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
    "android.activity",
    "android.compose.*",
    "shared.napier",

    "kotlin.coroutines.core",

    ":sharedMobileTest",
    ":modules:androidUi",
    ":modules:utils",
    ":modules:features:call:callFeature",
    ":modules:features:login",
    ":modules:features:more",
    ":modules:features:welcome",
    ":modules:features:myProfile",
    ":modules:features:chatList:chatListFeature",
    ":modules:features:experts:expertsFeature",
    ":modules:features:userChat:userChatFeature",
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
        val optIns = listOf(
            "androidx.compose.ui.ExperimentalComposeUiApi",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "com.google.accompanist.pager.ExperimentalPagerApi",
            "androidx.compose.material.ExperimentalMaterialApi",
            "androidx.compose.animation.ExperimentalAnimationApi",
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