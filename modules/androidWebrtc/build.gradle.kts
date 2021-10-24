plugins {
    id("com.android.library")
    kotlin("android")
}

libDependencies(
    "android.compose.ui",
    "android.webrtc",

    "shared.napier",
    ":modules:atomic",
    ":modules:models",
    ":modules:features:call:callFeature",
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