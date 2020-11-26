rootProject.name = "WELL"

private enum class Env {
    Full,
    Server,
    Mobile,
}
val serverPaths = listOf(
    ":server"
)
val mobilePaths = listOf(
    ":shared",
    ":auth",
    ":utils",
    ":androidApp"
)
(when (Env.Full) {
    Env.Full -> serverPaths + mobilePaths
    Env.Mobile -> mobilePaths
    Env.Server -> serverPaths
} + listOf(
    ":serverModels"
)).forEach {
    include(it)
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
//        maven {
//            name = "ViewBindingPropertyDelegate"
//            setUrl("https://dl.bintray.com/kirich1409/maven")
//        }
    }
    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            val gradlePluginVersion: String by settings
            when {
                pluginId == "com.android" ->
                    useModule("com.android.tools.build:gradle:${gradlePluginVersion}")
            }
        }
    }
}
