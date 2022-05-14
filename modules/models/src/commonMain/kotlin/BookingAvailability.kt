package com.well.modules.models

import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.today
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
class BookingAvailability(
    val availabilityId: Availability.Id,
    override val startInstant: Instant,
    override val durationMinutes: Int,
) : AvailabilityInfo {
    companion object {
        fun createWithAvailabilities(
            availabilities: List<Availability>,
            days: Int,
            minInterval: Duration,
        ) = LocalDate.today().let { today ->
            List(days) {
                today.daysShift(it)
            }.map { day ->
                day to availabilities
                    .mapDayAvailabilities(day, minInterval)
                    .map { availability ->
                        BookingAvailability(
                            availabilityId = availability.id,
                            startInstant = availability.startInstant,
                            durationMinutes = availability.durationMinutes,
                        )
                    }
            }.filter { it.second.isNotEmpty() }
        }
    }
}