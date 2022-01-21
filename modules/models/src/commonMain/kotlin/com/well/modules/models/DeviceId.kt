package com.well.modules.models

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class DeviceId(private val value: String) {
    override fun toString() = value

    object ColumnAdapter: com.squareup.sqldelight.ColumnAdapter<DeviceId, String> {
        override fun decode(databaseValue: String): DeviceId =
            DeviceId(databaseValue)

        override fun encode(value: DeviceId): String =
            value.value
    }
}