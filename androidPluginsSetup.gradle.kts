import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin

object Constants {
    val javaVersion = JavaVersion.VERSION_11
    const val group = "com.well"
    const val version = "1.0-SNAPSHOT"
}

val gradlePluginVersion = System.getProperty("gradlePluginVersion")!!
subprojects {
    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            setCompileSdkVersion(30)
            buildToolsVersion = "30.0.3"

            defaultConfig {
                minSdk = 23
                targetSdk = 30
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
    if (gradlePluginVersion.first() == '7' && !version("kotlin").startsWith("1.5")) {
        if (
            listOf(
                ":modules:models",
                ":modules:utils",
                ":modules:napier",
                ":modules:atomic",
                ":modules:annotations",
                ":sharedMobile",
            ).contains(path)
        ) {
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
        } else {
            println("not included $path")
        }
    }
}