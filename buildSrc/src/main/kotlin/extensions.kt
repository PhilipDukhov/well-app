import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.plugins.ExtraPropertiesExtension

private fun ExtraPropertiesExtension.mapAt(
    path: String,
    skipLast: Boolean
): Pair<LinkedHashMap<*, *>, String> {
    var components = path.split('.')
    var map = this["Libs"] as LinkedHashMap<*, *>
    val last = components.last()
    for (component in components.dropLast(if (skipLast) 1 else 0)) {
        map = map[component] as LinkedHashMap<*, *>
    }
    return Pair(map, last)
}

fun ExtraPropertiesExtension.libsAt(path: String) =
    mapAt(path, false).first
        .values
        .mapNotNull {
            when (it) {
                is String -> it
                is GStringImpl -> it.toString()
                else -> null
            }
        }

fun ExtraPropertiesExtension.libAt(path: String): Any {
    val (map, last) = mapAt(path, true)
    return when (val lastValue = map[last]) {
        is String -> lastValue
        is GStringImpl -> lastValue.toString()
        else -> throw NoSuchFieldException()
    }
}