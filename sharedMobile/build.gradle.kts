import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    if (withAndroid) {
        id("com.android.library")
    }
    kotlin("kapt")
}

val generatedKotlinSources: String = "$projectDir/src/gen/kotlin"

kapt {
    javacOptions {
        option("-Akapt.kotlin.generated=$generatedKotlinSources")
    }
}

kotlin {
    androidWithAndroid()
    val frameworkName = "SharedMobile"
    iosWithSimulator {
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

        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:annotations",
                ":modules:db:mobileDb",
                ":modules:db:chatMessagesDb",
                ":modules:db:usersDb",
                ":modules:utils:flowUtils",
                ":modules:utils:kotlinUtils",
                ":modules:networking",
                ":modules:features:login:loginHandlers",
                ":modules:features:call:callHandlers",
                ":modules:features:chatList:chatListHandlers",
                ":modules:features:experts:expertsHandlers",
                ":modules:features:userChat:userChatHandlers",

                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "ktor.client.core",
                "ktor.client.logging",
                "sqldelight.coroutinesExtensions",
                "shared.napier",
                "kotlin.datetime",
            )

            // Workaround for lack of Kapt support in multiplatform project:
            if (withAndroid) {
                dependencies.add("kapt", project(":modules:annotationProcessor"))
            }
            kotlin.srcDir(generatedKotlinSources)
        }
        if (withAndroid) {
            val androidMain by getting {
            }
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.coroutines.core-strictly",
            )
        }
    }
}
