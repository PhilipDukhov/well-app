rootProject.name = "WELL"

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
    }
    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            if (pluginId == "com.android" || pluginId == "kotlin-android-extensions") {
                useModule("com.android.tools.build:gradle:4.0.1")
            }
            if (pluginId.startsWith("com.avito.android")) {
                val artifact = pluginId.replace("com.avito.android.", "")
                useModule("com.avito.android:$artifact:2020.10")
            }
        }
    }
}



