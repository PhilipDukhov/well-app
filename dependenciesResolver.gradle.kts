enum class Executor {
    AndroidStudio,
    Idea,
}

val executor = try {
    Executor.valueOf(System.getProperty("idea.platform.prefix"))
} catch (t: Throwable) {
    Executor.Idea
}

val properties = java.io.File("$rootDir/gradle.properties").takeIf { it.exists() }
    ?.inputStream()?.use { java.util.Properties().apply { load(it) } }
    ?: error("gradle.properties not found $rootDir/gradle.properties")

val gradlePluginVersion = properties[
   if (executor == Executor.Idea) "gradlePluginVersion_Idea"
   else "gradlePluginVersion_As"
]!! as String

System.setProperty("gradlePluginVersion", gradlePluginVersion)
extra["gradlePluginVersion"] = gradlePluginVersion