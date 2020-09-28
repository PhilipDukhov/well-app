import org.codehaus.groovy.runtime.GStringImpl
import org.gradle.api.plugins.ExtraPropertiesExtension

fun ExtraPropertiesExtension.libAt(path: String): String {
    val components = path.split('.')
    var map = this["Libs"] as LinkedHashMap<*, *>
    val last = components.last()
    for (component in components.dropLast(1)) {
        map = map[component] as LinkedHashMap<*, *>
    }
    return when (val lastValue = map[last]) {
        is String -> lastValue
        is GStringImpl -> lastValue.toString()
        else -> throw NoSuchFieldException()
    }
}