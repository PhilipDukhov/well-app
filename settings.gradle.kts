rootProject.name = "WELL"

listOf(
    "androidApp",
    "server",
    "sharedMobile",
    "xModules:models",
    "xModules:atomic",
    "xModules:napier",
    "xModules:utils"
).forEach {
    include(":$it")
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
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
