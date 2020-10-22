import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.avito.android.plugin.build_param_check.CheckMode.FAIL
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude

val javaVersion = JavaVersion.VERSION_1_8
group = "com.well"
version = "1.0-SNAPSHOT"

buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
    }

    apply(from = "dependencies.gradle")
    val libs = extra.libsAt("build")
    dependencies {
        libs.forEach { classpath(it) }
        classpath("com.github.ben-manes:gradle-versions-plugin:0.33.0")
    }
}

repositories {
    mavenCentral()
}

plugins {
    id("com.avito.android.buildchecks")
}

buildChecks {
    enableByDefault = true

    /**
     * Android build tools uses android.jar ($ANDROID_HOME/platforms/android-<compileSdkVersion>/android.jar).
     * The version can be specified only without a revision (#117789774). Different revisions lead to Gradle remote cache misses
     */
    androidSdk {
        enabled = false
        compileSdkVersion = 30
        revision = 1
    }

    /**
     * The Java version can influence the output of the Java compiler. It leads to Gradle remote cache misses.
     */
    javaVersion {
        version = javaVersion
    }

    /**
     * If two Android modules use the same package, their R classes will be merged. While merging,
     * it can unexpectedly override resources. It happens even with android.namespacedRClass.
     */
    uniqueRClasses {
        enabled = false
    }

    /**
     * On macOs java.net.InetAddress#getLocalHost()
     * invocation can last up to 5 seconds instead of milliseconds (thoeni.io/post/macos-sierra-java).
     */
    macOSLocalhost { }

    /**
     * Dynamic versions, such as “2.+”, and snapshot versions force Gradle to check them on a remote server.
     * It slows down a configuration time and makes build less reproducible.
     */
    dynamicDependencies { }

    /**
     * Gradle can run multiple daemons for many reasons.
     * If you use buildSrc in the project with standalone Gradle wrapper, this check will verify common problems to reuse it.
     */
    gradleDaemon { }

    /**
     * This check verifies that all KAPT annotation processors support incremental annotation processing if it is enabled (kapt.incremental.apt=true).
     * Because if one of them does not support it then whole incremental annotation processing won’t work at all
     */
    incrementalKapt {
        mode = FAIL
    }
}

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
    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            setCompileSdkVersion(30)
            buildToolsVersion = "30.0.0"

            defaultConfig {
                minSdkVersion(23)
                targetSdkVersion(30)
            }
            buildTypes {
                getByName("release") {
                    isMinifyEnabled = false
                }
            }
            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }
    }

    apply(from = "${rootDir}/dependencies.gradle")
}

apply(plugin = "com.github.ben-manes.versions")

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
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

configurations.all {
    resolutionStrategy.eachDependency {
        when (requested.group to requested.name) {
            "org.jetbrains.kotlin" to "kotlin-reflect" -> useVersion(extra.version("kotlin"))
        }
    }
}

val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.39.0") {
        // need to exclude standard ruleset to use only diktat rules
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }

    // diktat ruleset
    ktlint("org.cqfn.diktat:diktat-rules:0.1.2")
}

val outputDir = "${project.buildDir}/reports/diktat/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))
val checkLocations = listOf(
    "androidApp",
    "androidLintRules/src/main/java/com/well/androidlintrules/detectors"
).map { "$it/**/*.kt" }

val diktatCheck by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path to sources that should be checked here
    args = checkLocations
}

val diktatFix by tasks.creating(JavaExec::class) {
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    main = "com.pinterest.ktlint.Main"

    // specify proper path to sources that should be checked here
    args = listOf("-F") + checkLocations
}