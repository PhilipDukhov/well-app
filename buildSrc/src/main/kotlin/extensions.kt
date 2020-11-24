import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.internal.impldep.com.amazonaws.services.kms.model.NotFoundException

private fun ExtraPropertiesExtension.mapAt(
    path: String,
    skipLast: Boolean
): Pair<LinkedHashMap<*, *>, String> {
    val components = path.split('.')
    var map = this["Libs"]!!.toLinkedHashMap()
    val last = components.last()
    for (component in components.dropLast(if (skipLast) 1 else 0)) {
        map = map[component]!!.toLinkedHashMap()
    }
    return Pair(map, last)
}

fun ExtraPropertiesExtension.libsAt(path: String) =
    mapAt(path, false)
        .first
        .values
        .map {
            when (it) {
                is String -> it
                is GStringImpl -> it.toString()
                else -> throw NotFoundException("$it")
            }
        }

fun ExtraPropertiesExtension.libAt(path: String): String {
    val (map, last) = mapAt(path, true)
    return when (val lastValue = map[last]) {
        is String -> lastValue
        is GStringImpl -> lastValue.toString()
        else -> throw NoSuchFieldException()
    }
}

fun ExtraPropertiesExtension.libsAt(paths: List<String>): List<String> =
    paths.map { libAt(it) }


fun ExtraPropertiesExtension.version(path: String) = (this["Versions"]!!.toLinkedHashMap()[path] as String?)!!

fun Any.toLinkedHashMap(): LinkedHashMap<*, *> = this as LinkedHashMap<*, *>
