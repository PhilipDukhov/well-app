import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.gradle.kotlin.dsl.add
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

private fun Project.mapAt(
    path: String,
    skipLast: Boolean
): Pair<LinkedHashMap<*, *>, String> {
    val components = path.split('.')
    var map = properties["Libs"]!!.toLinkedHashMap()
    val last = components.last()
    for (component in components.dropLast(if (skipLast) 1 else 0)) {
        map = map[component]!!.toLinkedHashMap()
    }
    return Pair(map, last)
}

fun Project.libsAt(path: String) =
    mapAt(path, false)
        .first
        .values
        .map {
            when (it) {
                is String -> it
                is GStringImpl -> it.toString()
                else -> throw java.lang.ClassNotFoundException("$it")
            }
        }

fun Project.libAt(path: String): String {
    val (map, last) = mapAt(path, true)
    return when (val lastValue = map[last]) {
        is String -> lastValue
        is GStringImpl -> lastValue.toString()
        else -> throw NoSuchFieldException()
    }
}

fun Project.libsAt(paths: List<String>): List<String> =
    paths.map { libAt(it) }


fun Project.version(path: String) =
    (properties["Versions"]!!.toLinkedHashMap()[path] as String?)!!

fun Any.toLinkedHashMap(): LinkedHashMap<*, *> = this as LinkedHashMap<*, *>

fun KotlinSourceSet.libDependencies(vararg libs: String) =
    dependencies {
        (this as DefaultKotlinDependencyHandler).apply {
            project.customDependencies(libs.asList())
                .forEach {
                    when (it) {
                        is Dependency.Implementation -> {
                            implementation(it.dependencyNotation) {
                                it.strictVersion?.let {
                                    version { strictly(it) }
                                }
                            }
                        }
                        is Dependency.Module -> {
                            implementation(project(it.name))
                        }
                    }
                }
        }
    }

fun Project.libDependencies(vararg libs: String) =
    customDependencies(libs.asList())
        .forEach {
            when (it) {
                is Dependency.Implementation -> {
                    dependencies.add("implementation", it.dependencyNotation) {
                        it.strictVersion?.let {
                            version { strictly(it) }
                        }
                    }
                }
                is Dependency.Module -> {
                    dependencies.add("implementation", project(it.name))
                }
            }
        }

sealed class Dependency {
    data class Implementation(
        val dependencyNotation: String,
        val strictVersion: String? = null
    ): Dependency()
    data class Module(
        val name: String
    ): Dependency()
}

private fun Project.customDependencies(libs: List<String>): List<Dependency> =
    libs.map {
        when {
            it == "kotlin.coroutines.core" ->
                Dependency.Implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core", "1.3.9-native-mt-2")
            it.startsWith(":") ->
                Dependency.Module(it)
            else ->
                Dependency.Implementation(libAt(it))
        }
    }