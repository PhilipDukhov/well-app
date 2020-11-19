rootProject.name = "WELL"

include(":androidLintRules")
include(":androidApp")
include(":shared")
include(":serverModels")
include(":server")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
        maven {
            name = "Avito bintray"
            setUrl("https://dl.bintray.com/avito/maven")
        }
        maven {
            name = "ViewBindingPropertyDelegate"
            setUrl("https://dl.bintray.com/kirich1409/maven")
        }
    }
    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            val gradlePluginVersion: String by settings
            when {
                pluginId == "com.android" || pluginId == "kotlin-android-extensions" ->
                    useModule("com.android.tools.build:gradle:${gradlePluginVersion}")

                pluginId.startsWith("com.avito.android") -> {
                    val artifact = pluginId.replace("com.avito.android.", "")
                    useModule("com.avito.android:$artifact:2020.28")
                }
            }
        }
    }
}
