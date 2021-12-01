plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    if (withAndroid) {
        id("com.android.library")
    }
}

kotlin {
    androidWithAndroid()
    val frameworkName = "SharedMobile"
    iosWithSimulator(project = project) {
        binaries {
            framework(frameworkName) {
                freeCompilerArgs += listOf("-Xobjc-generics")
            }
        }
    }
    cocoapods {
        framework {
            baseName = frameworkName
        }
        summary = frameworkName
        homepage = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")
    }
    exportIosModules(project)
    sourceSets {
        optIns(OptIn.Coroutines)
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:db:mobileDb",
                ":modules:db:chatMessagesDb",
                ":modules:db:usersDb",
                ":modules:db:meetingsDb",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                ":modules:networking",
                ":modules:features:login:loginHandlers",
                ":modules:features:myProfile:myProfileHandlers",
                ":modules:features:call:callHandlers",
                ":modules:features:chatList:chatListHandlers",
                ":modules:features:experts:expertsHandlers",
                ":modules:features:userChat:userChatHandlers",

                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "sqldelight.coroutinesExtensions",
                "shared.napier",
                "kotlin.datetime",
            )
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
