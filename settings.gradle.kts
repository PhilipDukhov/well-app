rootProject.name = "WELL"

apply(from = "dependenciesResolver.gradle.kts")
val withAndroid = System.getProperty("withAndroid")!!.toBoolean()
val kotlinVersion = System.getProperty("kotlinVersion")!!

val modules = mutableSetOf(
    "sharedMobile",
    "modules:annotations",
    "modules:annotationProcessor",
    "modules:models",
    "modules:atomic",
    "modules:napier",
    "modules:utils",
    "modules:db",
    "modules:dbHelper",
)
if (withAndroid) {
    modules.add("androidApp")
}
if (kotlinVersion.startsWith("1.5")) {
    modules.add("server")
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
            when {
                pluginId == "com.android" -> {
                    println("wtf $pluginId")
                    useModule("com.android.tools.build:gradle:${System.getProperty("gradlePluginVersion")}")
                }
            }
        }
    }
}
