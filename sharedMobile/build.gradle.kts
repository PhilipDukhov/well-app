plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlinx-serialization")
}

kotlin {
    android()
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
        ":serverModels"
    ).map { project(it) }
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework> {
            iosExportModules.forEach {
                export(it)
            }
        }
    }
    sourceSets {
        usePredefinedExperimentalAnnotations()

        val commonMain by getting {
            libDependencies(
                ":serverModels",
                ":utils",
                "kotlin.serializationJson",
                "napier",
                "ktor.client.core",
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
            dependencies {
                iosExportModules.forEach {
                    api(it)
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            libDependencies(
                "webrtc",
                "android.material",
                "android.activity",
                "android.compose.accompanist.coil",
                "ktor.client.engine.cio",
                "kotlin.coroutines.playServices"
            )
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.12")
            }
        }
        val iosMain by getting {
            libDependencies(
                "ktor.client.engine.ios"
            )
        }
    }
}
