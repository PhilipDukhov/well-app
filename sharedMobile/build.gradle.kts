import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("kotlinx-serialization")
    if (withAndroid) {
        id("com.android.library")
    }
    kotlin("kapt")
    id("com.codingfeline.buildkonfig")
}

val generatedKotlinSources: String = "$projectDir/src/gen/kotlin"

kapt {
    javacOptions {
        option("-Akapt.kotlin.generated=$generatedKotlinSources")
    }
}

buildkonfig {
    packageName = "com.well.sharedMobile"

    defaultConfigs {
        val dotEnv = DotEnv(project)
        mapOf(
            "google_web_client_id" to dotEnv.googleWebClientIdFull,
            "apple_server_client_id" to dotEnv["APPLE_SEVER_CLIENT_ID"],
            "apple_auth_redirect_url" to dotEnv["APPLE_AUTH_REDIRECT_URL"],
        ).forEach {
            buildConfigField(STRING, it.key, it.value)
        }
    }
}

kotlin {
    androidWithAndroid()
    val frameworkName = project.name.capitalize()
    ios() {
        binaries {
            framework(frameworkName) {
                freeCompilerArgs += listOf("-Xobjc-generics")
            }
        }
    }
    cocoapods {
        this.frameworkName = frameworkName
        summary = frameworkName
        homepage = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")
    }
    val iosExportModules = listOf(
        ":modules:models",
        ":modules:napier",
        ":modules:utils",
    ).map { project(it) }
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            iosExportModules.forEach {
                export(it)
            }
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
                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "ktor.client.core",
                "ktor.client.logging",
                "sqldelight.coroutinesExtensions",
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
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        if (withAndroid) {
            val androidMain by getting {
                libDependencies(
                    "webrtc",
                    "facebookLogin",
                    "google.playServicesAuth",
                    "android.material",
                    "android.activity",
                    "android.browser",
                    "ktor.client.engine.cio",
                    "kotlin.coroutines.playServices",
                )
            }
            val androidTest by getting {
                libDependencies(
                    "tests.junit",
                )
                dependencies {
                    implementation(kotlin("test-junit"))
                }
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
