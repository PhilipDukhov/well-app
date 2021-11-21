@file:UseSerializers(LocalTimeAsStringSerializer::class)

package com.well.modules.models

import com.well.modules.models.date.dateTime.LocalTime
import com.well.modules.models.date.dateTime.atTime
import com.well.modules.models.date.dateTime.plus
import com.well.modules.models.date.dateTime.toLocalDate
import com.well.modules.models.date.dateTime.toLocalTime
import com.well.modules.models.date.dateTime.today
import com.well.modules.models.date.dateTime.weekend
import com.well.modules.models.date.dateTime.workday
import com.well.modules.models.serializers.LocalTimeAsStringSerializer
import kotlinx.datetime.Clock
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

interface AvailabilityInfo {
    val startInstant: Instant
    val durationMinutes: Int

    val startDay get() = startInstant.toLocalDate(TimeZone.currentSystemDefault())
    val startTime get() = startInstant.toLocalTime(TimeZone.currentSystemDefault())
    val endTime get() = startTime.plus(Duration.minutes(durationMinutes))

    val intervalString: String
        get() = "$startTime-$endTime"
}

@Serializable
data class Availability(
    val id: Id,
    override val startInstant: Instant,
    override val durationMinutes: Int,
    val repeat: Repeat,
): AvailabilityInfo {
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

        object ColumnAdapter : com.squareup.sqldelight.ColumnAdapter<Id, Long> {
            override fun decode(databaseValue: Long): Id =
                Id(databaseValue)

            override fun encode(value: Id): Long =
                value.value
        }
    }

    fun copy(startTime: LocalTime) = copy(
        startInstant = startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
    )

    internal fun startInstantAtDay(startDay: LocalDate) =
        startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
}

fun List<Availability>.mapDayAvailabilities(
    day: LocalDate,
    minInterval: Duration = Duration.ZERO,
): List<Availability> {
    val now = Clock.System.now()
    if (day < LocalDate.today()) return listOf()
    return filter { availability ->
        if (day < availability.startDay) {
            return@filter false
        }
        if (day == availability.startDay) {
            return@filter true
        }
        when (availability.repeat) {
            Repeat.None -> {
                false
            }
            Repeat.Weekends -> {
                day.dayOfWeek.weekend
            }
            Repeat.Workdays -> {
                day.dayOfWeek.workday
            }
            Repeat.Weekly -> {
                availability.startDay.dayOfWeek == day.dayOfWeek
            }
        }
    }
        .map {
            it.copy(startInstant = it.startInstantAtDay(day))
        }
        .filter {
            it.startInstant + minInterval > now
        }
}

