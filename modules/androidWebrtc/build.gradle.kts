plugins {
    id("com.android.library")
    kotlin("android")
}

libDependencies(
    "android.compose.core.ui",
    "android.webrtc",

    "shared.napier",
    ":modules:atomic",
    ":modules:models",
    ":modules:features:call:callFeature",
)

dependencies {
    coreLibraryDesugaring()
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = project.version("compose")
    }
}