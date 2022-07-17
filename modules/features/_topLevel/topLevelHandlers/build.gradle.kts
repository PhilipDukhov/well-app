plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    android()
    val frameworkName = "SharedMobile"
    iosWithSimulator(
        project = project,
        config = {
            binaries {
                framework(frameworkName) {
                    @Suppress("SuspiciousCollectionReassignment")
                    freeCompilerArgs += listOf("-Xobjc-generics")
                }
            }
        },
        cocoapodsFrameworkName = frameworkName,
    )

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
                ":modules:features:calendar:calendarHandlers",
                ":modules:features:notifications",

                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "sqldelight.coroutinesExtensions",
                "shared.napier",
                "shared.okio",
                "kotlin.datetime",
            )
        }
        val androidMain by getting {
            libDependencies(
                "firebase.messaging",
            )
            dependencies {
                implementation(dependencies.platform(libAt("firebase.bom")))
            }
        }
        findByName("iosMain")?.run {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
