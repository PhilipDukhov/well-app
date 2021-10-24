import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("kotlinx-serialization")
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
    val frameworkName = project.name.capitalize()
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
    val iosExportModules = listOf(
        ":modules:models",
        ":modules:utils",
        ":modules:flowHelper",
    ).map { project(it) }
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
            libDependencies(
                ":modules:atomic",
                ":modules:annotations",
                ":modules:db:mobileDb",
                ":modules:db:chatMessagesDb",
                ":modules:db:usersDb",
                ":modules:flowHelper",
                ":modules:networking",
                ":modules:viewHelpers",
                ":modules:features:call",
                ":modules:features:login",
                ":modules:features:chatList",
                ":modules:features:experts",
                ":modules:features:more",
                ":modules:features:myProfile",
                ":modules:features:userChat",
                ":modules:features:welcome",

                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "ktor.client.core",
                "ktor.client.logging",
                "sqldelight.coroutinesExtensions",
                "shared.napier",
                "shared.datetime",
            )
            dependencies {
                iosExportModules.forEach {
                    api(it)
                }
            }

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
        }
    }
}
