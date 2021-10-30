package com.well.modules.db.helper

import com.squareup.sqldelight.ColumnAdapter

@Suppress("FunctionName")
inline fun <reified T : Enum<T>> SetEnumColumnAdapter() =
    SetEnumColumnAdapter(enumValues<T>())

class SetEnumColumnAdapter<T : Enum<T>>(
    private val enumValues: Array<out T>,
) : ColumnAdapter<Set<T>, String> {
    override fun decode(databaseValue: String): Set<T> =
        if (databaseValue.isNotEmpty())
            databaseValue
                .split(",")
                .mapTo(HashSet()) { value -> enumValues.first { it.name == value } }
        else setOf()

    override fun encode(value: Set<T>) =
        value.joinToString(separator = ",") { it.name }
}