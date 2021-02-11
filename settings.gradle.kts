rootProject.name = "WELL"

private enum class Env {
    Full,
    Server,
    Mobile,
}
val serverPaths = listOf(
    "server"
)
val mobilePaths = listOf(
    "sharedMobile",
//    "auth",
    "utils",
    "androidApp"
)
(when (Env.Full) {
    Env.Full -> serverPaths + mobilePaths
    Env.Mobile -> mobilePaths
    Env.Server -> serverPaths
} + listOf(
    "serverModels"
)).forEach {
    include(":$it")
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            val gradlePluginVersion: String by settings
            when {
                pluginId == "com.android" ->
                    useModule("com.android.tools.build:gradle:$gradlePluginVersion")
            }
        }
    }
}
