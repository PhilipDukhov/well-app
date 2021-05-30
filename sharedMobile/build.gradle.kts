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
        license = "-"
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
                ":modules:db",
                "kotlin.serializationJson",
                "kotlin.coroutines.core",
                "kotlin.stdLib",
                "ktor.client.core",
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
                    "android.compose.accompanist.coil",
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
            )
        }
    }
}
