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
        optIns()
        val commonMain by getting {
            libDependencies(
                ":modules:atomic",
                ":modules:models",
                ":modules:features:myProfile:myProfileHandlers",
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
        }
    }
}
