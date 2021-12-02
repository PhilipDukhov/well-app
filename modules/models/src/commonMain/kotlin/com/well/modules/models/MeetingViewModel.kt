package com.well.modules.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class MeetingViewModel(
    val id: Meeting.Id,
    override val startInstant: Instant,
    override val durationMinutes: Int,
    val user: User?,
): AvailabilityInfo {
    companion object

    val title = "Video call"

    val status get() = when {
        startInstant > Clock.System.now() -> Status.Upcoming
        endInstant < Clock.System.now() -> Status.Past
        else -> Status.Ongoing
    }

    enum class Status {
        Upcoming,
        Ongoing,
        Past,
        ;
    }
}