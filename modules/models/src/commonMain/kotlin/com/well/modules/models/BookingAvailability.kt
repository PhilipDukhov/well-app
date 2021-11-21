package com.well.modules.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class BookingAvailability(
    val availabilityId: Availability.Id,
    override val startInstant: Instant,
    override val durationMinutes: Int,
): AvailabilityInfo