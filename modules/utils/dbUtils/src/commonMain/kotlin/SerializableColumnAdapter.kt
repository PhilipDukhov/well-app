package com.well.modules.utils.dbUtils

import com.squareup.sqldelight.ColumnAdapter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Suppress("FunctionName")
inline fun <reified T : Any> SerializableColumnAdapter() =
    SerializableColumnAdapter(Json.serializersModule.serializer<T>())

class SerializableColumnAdapter<T : Any>(
    private val serializer: KSerializer<T>,
) : ColumnAdapter<T, String> {
    override fun decode(databaseValue: String): T = Json.decodeFromString(serializer, databaseValue)
    override fun encode(value: T) = Json.encodeToString(serializer, value)
}