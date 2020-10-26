import org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationToRunnableFiles

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    sourceSets {
        val jvmMain by getting {
            kotlin.srcDir("src")
            resources.srcDir("resources")

            dependencies {
                implementation(project(":serverModels"))
                listOf(
                    "kotlin.serializationJson",
                    "kotlin.stdLib",
                    "ktor.netty",
                    "ktor.core",
                    "ktor.auth",
                    "logback"
                ).forEach {
                    implementation(extra.libAt(it))
                }
            }
        }
    }
}

task<JavaExec>("run") {
    main = "com.well.server.ApplicationKt"
    val jvm by kotlin.targets.getting
    val main: KotlinCompilation<KotlinCommonOptions> by jvm.compilations
    val runtimeDependencies = (main as KotlinCompilationToRunnableFiles<KotlinCommonOptions>).runtimeDependencyFiles
    classpath = files(main.output.allOutputs, runtimeDependencies)
}