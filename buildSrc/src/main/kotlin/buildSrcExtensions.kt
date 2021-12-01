import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleVersionSelector
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.add
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
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
                            val forceApiModules = project
                                .properties["forceApiModules"] as List<String>
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

enum class OptIn(val deps: List<String>) {
    Basic(
        "kotlin.ExperimentalUnsignedTypes",
        "kotlin.contracts.ExperimentalContracts",
        "kotlin.time.ExperimentalTime",
        "kotlin.RequiresOptIn",
    ),
    Coroutines(
        "kotlinx.coroutines.InternalCoroutinesApi",
        "kotlinx.coroutines.FlowPreview",
        "kotlinx.coroutines.ExperimentalCoroutinesApi",
    ),
    Ktor(
        "io.ktor.utils.io.core.ExperimentalIoApi",
    ),
    Serialization(
        "kotlinx.serialization.ExperimentalSerializationApi"
    ),
    Compose(
        "androidx.compose.ui.ExperimentalComposeUiApi",
        "androidx.compose.foundation.ExperimentalFoundationApi",
        "com.google.accompanist.pager.ExperimentalPagerApi",
        "androidx.compose.material.ExperimentalMaterialApi",
        "androidx.compose.animation.ExperimentalAnimationApi",
        "coil.annotation.ExperimentalCoilApi",
    ),
    ;

    constructor(vararg deps: String) : this(deps.toList())
}

fun NamedDomainObjectCollection<KotlinSourceSet>.optIns(
    vararg optIns: OptIn,
) {
    all {
        optIns.mapToStrings()
            .forEach {
                languageSettings.optIn(it)
            }
    }
}

fun KotlinJvmOptions.optIns(
    vararg optIns: OptIn,
) {
    freeCompilerArgs = freeCompilerArgs + optIns.mapToStrings().map { "-Xopt-in=$it" }
}

private fun Array<out OptIn>.mapToStrings() =
    toList().plus(OptIn.Basic).flatMap(OptIn::deps).toSet()

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
    libs.flatMap { dep ->
        run list@{
            listOf(run single@{
                when {
                    dep.startsWith("test.") ->
                        Dependency.Test(dep)
                    dep.startsWith(":") ->
                        Dependency.Module(dep)
                    dep == "kotlin.coroutines.core-strictly" ->
                        Dependency.Implementation(
                            dependencyNotation = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                            strictVersion = version("kotlinCoroutines")
                        )
                    dep.endsWith(".*") -> {
                        return@list libsAt(dep.dropLast(2)).map(Dependency::Implementation)
                    }
                    else -> {
                        Dependency.Implementation(libAt(dep))
                    }
                }
            })
        }
    }

fun KotlinMultiplatformExtension.iosWithSimulator(
    project: Project? = null,
    config: KotlinNativeTarget.() -> Unit = {},
) {
    ios(configure = config)

    val platform = try {
        project?.let { it.extra["kotlin.native.cocoapods.platform"] as? String }
    } catch (t: Throwable) {
        null
    }

    if (platform == "iphonesimulator") {
        iosSimulatorArm64(configure = config)
        val iosMain by sourceSets.getting
        val iosSimulatorArm64Main by sourceSets.getting
        iosSimulatorArm64Main.dependsOn(iosMain)
    }
}

fun DependencyHandlerScope.coreLibraryDesugaring() =
    add("coreLibraryDesugaring", "com.android.tools:desugar_jdk_libs:1.1.5")

fun KotlinMultiplatformExtension.exportIosModules(project: Project) {
    @Suppress("UNCHECKED_CAST")
    val iosExportItems = project.properties["iosExportItems"] as List<String>
    val (modulesNames, libNames) = iosExportItems.partition { it.startsWith(':') }
    val iosExportModules = modulesNames.map { project.project(it) }
    targets.withType<KotlinNativeTarget> {
        binaries.withType<Framework> {
            iosExportModules.forEach {
                export(it)
            }
            libNames.forEach {
                export(project.libAt(it))
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets.apply {
        val commonMain by getting {
            libDependencies(modulesNames)
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

enum class ResolutionStrategy {
    Kotlin,
    Compose,
    Coroutines,
}

fun Project.subprojectsConfigurationsResolutionStrategy(strategies: Set<ResolutionStrategy>) {
    subprojects.plus(project).forEach {
        it.configurations.all {
            resolutionStrategy.eachDependency {
                strategies.forEach { strategy ->
                    when (strategy) {
                        ResolutionStrategy.Compose -> {
                            if (requested.group.startsWith("androidx.compose") && requested.group != "androidx.compose.material3") {
                                useVersion(project.version("compose"))
                            }
                        }
                        ResolutionStrategy.Coroutines -> {
                            when (requested.groupToName()) {
                                "org.jetbrains.kotlinx" to "kotlinx-coroutines-core" -> {
                                    useVersion(project.version("kotlinCoroutines"))
                                }
                            }
                        }
                        ResolutionStrategy.Kotlin -> {
                            if (requested.group == "org.jetbrains.kotlin") {
                                useVersion(project.version("kotlin"))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ModuleVersionSelector.groupToName() = group to name

fun RepositoryHandler.addSnapshots(uri: (Any) -> java.net.URI, project: Project) {
    if (executor == Executor.CocoapodsArm64Simulator) {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
    if (project.extra.properties["kotlin.native.binary.memoryModel"] as? String == "experimental") {
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
        maven {
            url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
        }
    }
}