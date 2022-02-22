package com.well.modules.utils.dbUtils

class SetEnumColumnAdapter<T : Enum<T>>(
    private val enumValues: Array<out T>,
) : SetColumnAdapter<T>(
    encodeT = {
        it.name
    },
    decodeT = { value ->
        enumValues.first { it.name == value }
    }
) {
    companion object {
        inline operator fun <reified T : Enum<T>> invoke() =
            SetEnumColumnAdapter(enumValues<T>())
    }
}