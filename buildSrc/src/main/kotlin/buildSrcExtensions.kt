import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinTargetContainerWithNativeShortcuts
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultKotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

private fun Project.mapAt(
    path: String,
    skipLast: Boolean,
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
                            @Suppress("UNCHECKED_CAST")
                            val forceApiModules =
                                project.properties["forceApiModules"] as List<String>
                            it.name.let { name ->
                                project(name).let { dep ->
                                    if (forceApiModules.contains(name)) {
                                        api(dep)
                                    } else {
                                        implementation(dep)
                                    }
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

enum class OptIn {
    Coroutines,
    Ktor,
}

fun NamedDomainObjectCollection<KotlinSourceSet>.optIns(
    vararg annotations: String,
    optIns: Set<OptIn> = emptySet(),
) {
    all {
        (optIns.flatMap {
            when (it) {
                OptIn.Coroutines -> listOf(
                    "kotlinx.coroutines.InternalCoroutinesApi",
                    "kotlinx.coroutines.FlowPreview",
                    "kotlinx.coroutines.ExperimentalCoroutinesApi",
                )
                OptIn.Ktor -> listOf(
                    "io.ktor.utils.io.core.ExperimentalIoApi",
                )
            }
        } + listOf(
            "kotlin.ExperimentalUnsignedTypes",
            "kotlin.contracts.ExperimentalContracts",
            "kotlin.time.ExperimentalTime",
            "kotlin.RequiresOptIn",
        ) + annotations.toList()).forEach {
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
        val strictVersion: String?,
    ) : Dependency() {
        constructor(dependencyNotation: String) : this(dependencyNotation, null)
    }

    data class Module(
        val name: String,
    ) : Dependency()

    data class Test(
        val dependencyNotation: String,
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

fun KotlinMultiplatformExtension.exportIosModules(project: Project) {
    @Suppress("UNCHECKED_CAST")
    val iosExportModulesNames = project.properties["iosExportModulesNames"] as List<String>
    val iosExportModules = iosExportModulesNames.map { project.project(it) }
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            iosExportModules.forEach {
                export(it)
            }
            export(project.libAt("shared.napier"))
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets.apply {
        val commonMain by getting {
            libDependencies(iosExportModulesNames)
        }
        val iosMain by getting {
            dependencies {
                iosExportModules.forEach {
                    api(it)
                }
            }
        }
    }
}

val composeOptIns = listOf(
    "androidx.compose.ui.ExperimentalComposeUiApi",
    "androidx.compose.foundation.ExperimentalFoundationApi",
    "com.google.accompanist.pager.ExperimentalPagerApi",
    "androidx.compose.material.ExperimentalMaterialApi",
    "androidx.compose.animation.ExperimentalAnimationApi",
    "coil.annotation.ExperimentalCoilApi",
)

//fun Project.enableDesugaring() {
//    dependencies {
//        coreLibraryDesugaring()
//    }
//    android {
//        compileOptions {
//            isCoreLibraryDesugaringEnabled = true
//        }
//    }
//}