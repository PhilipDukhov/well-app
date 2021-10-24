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
    val iosExportModulesNames = listOf(
        ":modules:features:login",
        ":modules:features:more",
        ":modules:features:welcome",
        ":modules:features:myProfile",
        ":modules:features:call:callFeature",
        ":modules:features:chatList:chatListFeature",
        ":modules:features:experts:expertsFeature",
        ":modules:features:userChat:userChatFeature",
        ":modules:models",
        ":modules:utils",
        ":modules:flowHelper",
        ":modules:viewHelpers",
    )
    val iosExportModules = iosExportModulesNames.map { project(it) }
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            iosExportModules.forEach {
                export(it)
            }
            export(libAt("shared.napier"))
        }
    }
    sourceSets {
        usePredefinedExperimentalAnnotations()

        val commonMain by getting {
            libDependencies(iosExportModulesNames)
            libDependencies(
                ":modules:atomic",
                ":modules:annotations",
                ":modules:db:mobileDb",
                ":modules:db:chatMessagesDb",
                ":modules:db:usersDb",
                ":modules:flowHelper",
                ":modules:viewHelpers",
                ":modules:networking",
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
                libDependencies(
                    "ktor.client.engine.cio",
                )
            }
        }
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios",
                "kotlin.coroutines.core-strictly",
            )
            dependencies {
                iosExportModules.forEach {
                    api(it)
                }
            }
        }
    }
}
