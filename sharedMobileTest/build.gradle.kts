import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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
                ":modules:models",
                "kotlin.datetime",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "shared.napier",
                "kotlin.datetime",
            )
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                )
            }
        }
        val iosMain by getting {
            libDependencies(
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
