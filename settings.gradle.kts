rootProject.name = "WELL"

apply(from = "dependenciesResolver.gradle.kts")
val withAndroid = System.getProperty("withAndroid")!!.toBoolean()

val modules = mutableSetOf(
    ":server",
    ":sharedMobile",
    ":modules:annotations",
    ":modules:annotationProcessor",
    ":modules:models",
    ":modules:atomic",
    ":modules:utils",
    ":modules:db:usersDb",
    ":modules:db:chatMessagesDb",
    ":modules:db:serverDb",
    ":modules:db:mobileDb",
    ":modules:db:helperDb",
    ":modules:flowHelper",
)
if (withAndroid) {
    modules.add("androidApp")
}

modules.forEach {
    include(it)
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
            when {
                pluginId == "com.android" -> {
                    useModule("com.android.tools.build:gradle:${System.getProperty("gradlePluginVersion")}")
                }
            }
        }
    }
}
