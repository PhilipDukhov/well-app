package com.well.modules.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Meeting(
    val id: Id,
    val startInstant: Instant,
    val durationMinutes: Int,
    val hostId: User.Id,
    val attendeeId: User.Id,
) {
    @Serializable
    @JvmInline
    value class Id(val value: Long) {
        override fun toString() = value.toString()

        object ColumnAdapter: com.squareup.sqldelight.ColumnAdapter<Id, Long> {
            override fun decode(databaseValue: Long): Id =
                Id(databaseValue)

            override fun encode(value: Id): Long =
                value.value
        }
    }
}