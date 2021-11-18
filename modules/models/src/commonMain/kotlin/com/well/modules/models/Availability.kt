@file:UseSerializers(LocalTimeAsStringSerializer::class)

package com.well.modules.models

import com.well.modules.models.date.dateTime.LocalTime
import com.well.modules.models.date.dateTime.atTime
import com.well.modules.models.date.dateTime.plus
import com.well.modules.models.date.dateTime.toLocalDate
import com.well.modules.models.date.dateTime.toLocalTime
import com.well.modules.models.serializers.LocalTimeAsStringSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.jvm.JvmInline
import kotlin.time.Duration

enum class Repeat {
    None,
    Weekly,
    Workdays,
    Weekends,
    ;

    val title get() = name.lowercase()

    companion object {
        val allCases = values().toList()
    }
}

@Serializable
data class Availability(
    val id: Id,
    val startInstant: Instant,
    val durationMinutes: Int,
    val repeat: Repeat,
) {
    constructor(
        id: Id,
        startDay: LocalDate,
        startTime: LocalTime,
        durationMinutes: Int,
        repeat: Repeat,
    ) : this(
        id,
        startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault()),
        durationMinutes,
        repeat
    )

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

    fun copy(startTime: LocalTime) = copy(
        startInstant = startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
    )

    fun copy(startDay: LocalDate) = copy(
        startInstant = startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
    )

    val startDay = startInstant.toLocalDate(TimeZone.currentSystemDefault())
    val startTime = startInstant.toLocalTime(TimeZone.currentSystemDefault())
    val endTime = startTime.plus(Duration.minutes(durationMinutes))

    val intervalString: String
        get() = "$startTime-$endTime"
}

