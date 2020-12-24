import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

object Constants {
    val javaVersion = JavaVersion.VERSION_1_8
    const val group = "com.well"
    const val version = "1.0-SNAPSHOT"
}

group = Constants.group
version = Constants.version

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
    }

    apply(from = "dependencies.gradle")
    val libs: List<String> = project.libsAt("build")
    dependencies {
        libs.forEach { classpath(it) }
    }
}

repositories {
    mavenCentral()
}

//plugins {
//    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.5"
//}

allprojects {
    @Suppress("UnstableApiUsage")
    repositories {
        jcenter()
        google()

        exclusiveContent {
            forRepository {
                google()
            }
            filter {
                includeGroupByRegex("androidx\\..+")
                includeGroupByRegex("com.android.*")
                includeGroupByRegex("com.google.android.+")
            }
        }
    }
    apply(from = "${rootDir}/dependencies.gradle")
}

subprojects {
    group = Constants.group
    version = Constants.version
    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            setCompileSdkVersion(30)
            buildToolsVersion = "30.0.2"

            defaultConfig {
                minSdkVersion(23)
                targetSdkVersion(30)
                versionCode = 1
                versionName = Constants.version
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
            compileOptions {
                sourceCompatibility = Constants.javaVersion
                targetCompatibility = Constants.javaVersion
            }
        }
    }
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper> {
        plugins.whenPluginAdded {
            extensions
                .findByType<com.android.build.gradle.LibraryExtension>()
                ?.apply {
                    sourceSets["main"]?.manifest?.srcFile("src/androidMain/AndroidManifest.xml")
                }
        }
    }
    plugins.withType<org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper> {
        this@subprojects.run {
            tasks {
                withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                    kotlinOptions {
                        jvmTarget = Constants.javaVersion.toString()
                    }
                }
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs += listOf(
            // jetpack compose
            "-Xallow-jvm-ir-dependencies", "-Xskip-prerelease-check",
            "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi"
        )
    }

    if (listOf(
            "serverModels",
            "utils",
            "auth",
            "shared"
        ).contains(name)
    ) {
        configurations {
            listOf(
                "androidTestApi",
                "androidTestDebugApi",
                "androidTestReleaseApi",
                "testApi",
                "testDebugApi",
                "testReleaseApi"
            ).forEach {
                create(it) {}
            }
        }
    }

    apply(from = "${rootDir}/dependencies.gradle")
}

configurations.all {
    resolutionStrategy.eachDependency {
        when (requested.group to requested.name) {
            "org.jetbrains.kotlin" to "kotlin-reflect" -> useVersion(project.version("kotlin"))
        }
    }
}

// ./gradlew -q -PallDepsNeeded=1 allDeps > deps.txt && open deps.txt
val allDepsNeeded: String? by project
if (allDepsNeeded != null) {
    subprojects {
        tasks.create<DependencyReportTask>("allDeps") {
        }
    }
}

// ./gradlew -q -PdiktatNeeded=1 diktatFix --stacktrace
//val diktatNeeded: String? by project
//if (diktatNeeded != null) {
//    apply(plugin = "com.github.ben-manes.versions") {
//        version = "0.1.5"
//    }
//    diktat {
//        inputs = files("androidApp/**/*.kt")
//    }
//}

// ./gradlew -q -PdependencyUpdatesNeeded=1 dependencyUpdates
val dependencyUpdatesNeeded: String? by project
if (dependencyUpdatesNeeded != null) {
    apply(plugin = "com.github.ben-manes.versions")

    fun isNonStable(version: String): Boolean {
        val stableKeyword =
            listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }

    tasks.named("dependencyUpdates", DependencyUpdatesTask::class.java).configure {
        // Example 1: reject all non stable versions
        rejectVersionIf {
            isNonStable(candidate.version)
        }

        // Example 2: disallow release candidates as upgradable versions from stable versions
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }

        // Example 3: using the full syntax
        resolutionStrategy {
            componentSelection {
                all {
                    if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                        reject("Release candidate")
                    }
                }
            }
        }

        checkForGradleUpdate = true
        outputFormatter = "json"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
}
