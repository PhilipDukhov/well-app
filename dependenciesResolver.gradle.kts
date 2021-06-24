import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties as JavaProperties
import org.gradle.api.initialization.ProjectDescriptor

val root = if (rootProject.name == "buildSrc") {
    rootDir.parent
} else {
    rootDir.absolutePath
}

enum class Executor {
    AndroidStudio,
    Idea,
    Cocoapods,
    Console,
}

val executor = Executor.AndroidStudio
//    try {
//    Executor.valueOf(System.getProperty("idea.platform.prefix"))
//} catch (t: Throwable) {
//    val dir = System.getProperty("user.dir").toString()
//    when {
//        dir.endsWith("Pods") -> Executor.Cocoapods
//        dir.endsWith("fastlane") -> Executor.AndroidStudio
//        else -> {
//            Executor.Console
//        }
//    }
//}

//val withAndroid = true
val withAndroid = executor == Executor.AndroidStudio && run {
    // Loads the local.properties file
    val localProperties = java.io.File("$root/local.properties").takeIf { it.exists() }
        ?.inputStream()?.use { java.util.Properties().apply { load(it) } }
        ?: error("Please create a local.properties file (sample in local.sample.properties).")

    // There need to be at least either sdk.dir or skip.android in the file
    if (localProperties["sdk.dir"] == null && localProperties["skipAndroid"] != "true") {
        error("local.properties: sdk.dir == null && skip.android != true")
    }
    localProperties["skipAndroid"] != "true"
}

System.setProperty("withAndroid", withAndroid.toString())

val properties = java.io.File("$rootDir/gradle.properties").takeIf { it.exists() }
    ?.inputStream()?.use { java.util.Properties().apply { load(it) } }
    ?: error("gradle.properties not found $rootDir/gradle.properties")

val gradlePluginVersion = if (executor != Executor.AndroidStudio) null
    else properties["gradlePluginVersion"]!! as String
if (gradlePluginVersion != null) {
    System.setProperty("gradlePluginVersion", gradlePluginVersion)
    extra["gradlePluginVersion"] = gradlePluginVersion
}
extra["withAndroid"] = withAndroid

println("executor:$executor withAndroid:$withAndroid gradlePluginVersion:$gradlePluginVersion")