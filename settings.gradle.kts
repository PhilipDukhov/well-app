rootProject.name = "WELL"

include(":androidLintRules")
include(":androidApp")
include(":shared")

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
            when {
                pluginId == "com.android" || pluginId == "kotlin-android-extensions" ->
                    useModule("com.android.tools.build:gradle:4.1.0")

                pluginId.startsWith("com.avito.android") -> {
                    val artifact = pluginId.replace("com.avito.android.", "")
                    useModule("com.avito.android:$artifact:2020.23")
                }
            }
        }
    }
}
