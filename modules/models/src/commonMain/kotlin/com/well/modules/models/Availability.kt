@file:UseSerializers(LocalTimeAsStringSerializer::class)

package com.well.modules.models

import com.well.modules.models.serializers.LocalTimeAsStringSerializer
import com.well.modules.models.date.dateTime.LocalTime
import com.well.modules.models.date.dateTime.atTime
import com.well.modules.models.date.dateTime.plus
import com.well.modules.models.date.dateTime.toLocalDate
import com.well.modules.models.date.dateTime.toLocalTime
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.time.Duration

enum class Repeat {
    None,
    Weekly,
    Workdays,
    Weekends,
    ;
}

typealias AvailabilityId = Int

@Serializable
data class Availability(
    val id: AvailabilityId,
    val startInstant: Instant,
    val durationMinutes: Int,
    val repeat: Repeat,
) {
    constructor(
        id: AvailabilityId,
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

    fun copy(startTime: LocalTime) = copy(
        startInstant = startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
    )

    fun copy(startDay: LocalDate) = copy(
        startInstant = startDay.atTime(startTime).toInstant(TimeZone.currentSystemDefault())
    )

    val startDay = startInstant.toLocalDate(TimeZone.currentSystemDefault())
    val startTime = startInstant.toLocalTime(TimeZone.currentSystemDefault())
    val endTime = startTime.plus(Duration.minutes(durationMinutes))

    val startTimeValid get() = startInstant >= Clock.System.now()
    val endTimeValid get() = durationMinutes > 0

    val intervalString: String
        get() = "$startTime-$endTime"
}

