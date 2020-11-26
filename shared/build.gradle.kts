import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("kotlinx-serialization")
}

kotlin {
    android()
    ios()
    cocoapods {
        frameworkName = "Shared"
        summary = frameworkName
        homepage = "-"
        license = "-"
        ios.deploymentTarget = project.version("iosDeploymentTarget")
    }
    sourceSets {
        val commonMain by getting {
            libDependencies(
                ":serverModels",
                ":utils",
                "kotlin.serializationJson",
                "napier",
                "oolong",
                "ktor.client.cio",
                "kotlin.coroutines.core",
                "kotlin.stdLib"
            )
        }
        val androidMain by getting {
            libDependencies(
                "kotlin.coroutines.playServices",
                "okhttp3"
            )
        }
        val iosMain by getting {
            libDependencies(
                "kotlin.serializationJson",
                "napier",
                "kotlin.stdLib",
                "oolong"
            )
        }
    }
}