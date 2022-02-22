import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin

object Constants {
    val javaVersion = JavaVersion.VERSION_11
    const val group = "com.well"
    const val version = "1.0-SNAPSHOT"
}

subprojects {
    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            setCompileSdkVersion(31)
            buildToolsVersion = "30.0.3"

            defaultConfig {
                minSdk = 23
                targetSdk = 31
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
}

subprojectsConfigurationsResolutionStrategy(ResolutionStrategy.Compose)