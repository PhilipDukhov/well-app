package com.well.modules.utils.dbUtils

import com.squareup.sqldelight.ColumnAdapter

open class SetColumnAdapter<T>(
    val encodeT: (T) -> String,
    val decodeT: (String) -> T,
) : ColumnAdapter<Set<T>, String> {
    override fun decode(databaseValue: String): Set<T> =
        if (databaseValue.isNotEmpty())
            databaseValue
                .split(",")
                .mapTo(HashSet()) { decodeT(it) }
        else setOf()

    override fun encode(value: Set<T>) =
        value.joinToString(separator = ",") { encodeT(it) }
}