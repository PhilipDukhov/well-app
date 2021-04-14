import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin

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
        libs.forEach { classpath(it) }
    }
}

repositories {
    mavenCentral()
}

val gradlePluginVersion = extra["gradlePluginVersion"] as String

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

val multiplatformModules = listOf(
    ":modules:models",
    ":modules:utils",
    ":modules:napier",
    ":modules:atomic",
    ":modules:annotations",
    ":sharedMobile",
)

subprojects {
    group = Constants.group
    version = Constants.version
    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            setCompileSdkVersion(30)
            buildToolsVersion = "30.0.3"

            defaultConfig {
                minSdkVersion(23)
                targetSdkVersion(30)
                versionCode = 102191720
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
            "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI",
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
        kotlinOptions {
            useIR = true
        }
    }

    if (gradlePluginVersion.first() == '7') {
        if (multiplatformModules.contains(path)) {
            configurations {
                listOf(
                    "androidTestApi",
                    "androidTestDebugApi",
                    "androidTestReleaseApi",
                    "testApi",
                    "testDebugApi",
                    "testReleaseApi",
                ).forEach {
                    create(it) {}
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
    apply(from = "dependencyUpdates.gradle.kts")
}
