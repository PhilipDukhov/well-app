rootProject.name = "WELL"

listOf(
    "androidApp",
    "server",
    "sharedMobile",
    "modules:models",
    "modules:atomic",
    "modules:napier",
    "modules:utils",
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
