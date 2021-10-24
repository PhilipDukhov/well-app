import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinDependencyHandler
import org.gradle.kotlin.dsl.add
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithNativeShortcuts
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

private fun Project.mapAt(
    path: String,
    skipLast: Boolean
): Pair<LinkedHashMap<*, *>, String> {
    val components = path.split('.')
    var map = properties["Libs"]?.toLinkedHashMap() ?: throw IllegalStateException("Libs missing")
    val last = components.last()
    for (component in components.dropLast(if (skipLast) 1 else 0)) {
        map = map[component]?.toLinkedHashMap()
            ?: throw IllegalStateException("Wrong path: $component missing at $path")
    }
    return Pair(map, last)
}

fun Project.libsAt(path: String): List<String> =
    mapAt(path, false)
        .first
        .values
        .fold(listOf()) { res, lib ->
            res + libsMapper(lib)
        }

private fun libsMapper(libs: Any): List<String> =
    when (libs) {
        is String -> listOf(libs)
        is GStringImpl -> listOf(libs.toString())
        is Collection<*> -> libs.fold(listOf()) { res, lib ->
            lib?.let { res + libsMapper(lib) } ?: res
        }
        is LinkedHashMap<*, *> -> libsMapper(libs.values)
        else -> throw ClassNotFoundException("$libs")
    }

fun Project.libAt(path: String): String {
    val (map, last) = mapAt(path, true)
    return when (val lastValue = map[last]) {
        is String -> lastValue
        is GStringImpl -> lastValue.toString()
        else -> throw NoSuchFieldException(path)
    }
}

fun Project.libsAt(paths: List<String>): List<String> =
    paths.map { libAt(it) }

fun Project.version(name: String) =
    (properties["Versions"]!!.toLinkedHashMap()[name] as String?)!!

fun Any.toLinkedHashMap(): LinkedHashMap<*, *> = this as LinkedHashMap<*, *>

fun KotlinSourceSet.libDependencies(vararg libs: String) =
    libDependencies(libs.asList())

fun KotlinSourceSet.libDependencies(libs: List<String>) =
    dependencies {
        (this as DefaultKotlinDependencyHandler).apply {
            project.customDependencies(libs)
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
                            when (val name = it.name) {
                                ":modules:atomic",
                                ":modules:viewHelpers",
                                ":modules:models",
                                -> {
                                    api(project(name))
                                }
                                else -> {
                                    implementation(project(name))
                                }
                            }
                        }
                        is Dependency.Test -> {
                            implementation(it.dependencyNotation)
                        }
                    }
                }
        }
    }

fun NamedDomainObjectCollection<KotlinSourceSet>.usePredefinedExperimentalAnnotations(
    vararg annotations: String
) {
    all {
        (annotations.toList() +
                listOf(
                    "kotlinx.coroutines.InternalCoroutinesApi",
                    "kotlinx.coroutines.FlowPreview",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "kotlin.ExperimentalUnsignedTypes",
                    "kotlin.contracts.ExperimentalContracts",
                    "kotlin.time.ExperimentalTime",
                    "io.ktor.util.InternalAPI",
                    "io.ktor.utils.io.core.ExperimentalIoApi",
                )).forEach {
            languageSettings.optIn(it)
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
                is Dependency.Test -> dependencies.add(
                    "testImplementation",
                    it.dependencyNotation
                )
            }
        }

sealed class Dependency {
    data class Implementation(
        val dependencyNotation: String,
        val strictVersion: String?
    ) : Dependency() {
        constructor(dependencyNotation: String) : this(dependencyNotation, null)
    }

    data class Module(
        val name: String
    ) : Dependency()

    data class Test(
        val dependencyNotation: String
    ) : Dependency()
}

private fun Project.customDependencies(libs: List<String>): List<Dependency> =
    libs.fold(listOf()) { result: List<Dependency>, dep ->
        when {
            dep.startsWith("test.") ->
                result + Dependency.Test(dep)
            dep.startsWith(":") ->
                result + Dependency.Module(dep)
            dep == "kotlin.coroutines.core-strictly" ->
                result + Dependency.Implementation(
                    "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                    version("kotlinCoroutines")
                )
            dep.endsWith(".*") -> {
                result + libsAt(dep.dropLast(2)).map(Dependency::Implementation)
            }
            else ->
                result + Dependency.Implementation(libAt(dep))
        }
    }

fun KotlinTargetContainerWithNativeShortcuts.iosWithSimulator(config: KotlinNativeTarget.() -> Unit = {}) {
    ios(configure = config)
//    iosSimulatorArm64(configure = config)
}

fun DependencyHandlerScope.coreLibraryDesugaring() =
    add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:1.1.5")
