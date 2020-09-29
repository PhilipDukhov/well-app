import com.android.build.gradle.*
import com.avito.android.plugin.build_param_check.CheckMode.FAIL

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
                minSdkVersion(16)
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