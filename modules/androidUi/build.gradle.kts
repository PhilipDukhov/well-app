plugins {
    id("com.android.library")
    kotlin("android")
}

dependencies {
    coreLibraryDesugaring()
}

libDependencies(
    "android.material",
    "android.appCompat",
    "android.activity",
    "android.compose.*",
    "android.webrtc",
    "android.dataStore",

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
    ":modules:utils:flowUtils",
    ":modules:networking",
    ":modules:features:more",
    ":modules:features:welcome",
    ":modules:features:myProfile",
    ":modules:features:call:callFeature",
    ":modules:features:login:loginFeature",
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
        freeCompilerArgs = freeCompilerArgs + composeOptIns.map { "-Xopt-in=$it" }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = project.version("compose")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + composeOptIns.map { "-Xopt-in=$it" }
    }
}