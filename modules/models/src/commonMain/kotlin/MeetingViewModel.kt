package com.well.modules.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class MeetingViewModel(
    val id: Meeting.Id,
    val state: Meeting.State,
    val isExpert: Boolean,
    override val startInstant: Instant,
    override val durationMinutes: Int,
    val otherUser: User,
) : AvailabilityInfo {
    companion object

    val waitingExpertResolution get() = isExpert && state == Meeting.State.Requested

    val title = when (state) {
        Meeting.State.Requested -> {
            if (isExpert) {
                "${otherUser.fullName} needs your help"
            } else {
                "Meeting requested"
            }
        }
        Meeting.State.Confirmed -> {
            "Meeting"
        }
        Meeting.State.Rejected -> {
            "Meeting rejected"
        }
        is Meeting.State.Canceled -> TODO()
    }

    val status
        get() = when {
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