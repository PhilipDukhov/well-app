object Constants {
    val javaVersion = JavaVersion.VERSION_11
    const val group = "com.well"
    const val version = "1.0-SNAPSHOT"
}

group = Constants.group
version = Constants.version

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    apply(from = "dependencies.gradle")
    val libs: List<String> = project.libsAt("build")
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        libs.forEach { classpath(it) }
    }
}

repositories {
    mavenCentral()
}

plugins {
    id("com.gradleup.auto.manifest") version "1.0.4"
}

autoManifest {
    packageName.set(Constants.group)
    applyRecursively.set(true)
}

allprojects {
    @Suppress("UnstableApiUsage")
    repositories {
        // still needed for webrtc
        @Suppress("JcenterRepositoryObsolete", "DEPRECATION")
        jcenter()

        google()
        mavenCentral()

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

if (withAndroid) {
    apply(from = "${rootDir}/androidPluginsSetup.gradle.kts")
}

subprojects {
    group = Constants.group
    version = Constants.version
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

    apply(from = "${rootDir}/dependencies.gradle")
    configurations.all {
        resolutionStrategy.eachDependency {
            when (requested.group to requested.name) {
                "org.jetbrains.kotlin" to "kotlin-reflect" -> useVersion(project.version("kotlin"))
            }
        }
    }
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

    tasks.named("dependencyUpdates", com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class.java).configure {
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
