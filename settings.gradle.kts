rootProject.name = "WELL"

val kotlinVersion: String by settings

val modules = mutableSetOf(
    "sharedMobile",
    "modules:annotations",
    "modules:annotationProcessor",
    "modules:models",
    "modules:atomic",
    "modules:napier",
    "modules:utils",
)
if (kotlinVersion.startsWith("1.5")) {
    modules.add("server")
} else {
    modules.add("androidApp")
}

modules.forEach {
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
