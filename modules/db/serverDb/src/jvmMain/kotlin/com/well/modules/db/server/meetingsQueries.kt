package com.well.modules.db.server

import com.well.modules.models.BookingAvailability
import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.utils.dbUtils.adaptedIntersectionRegex
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun MeetingsQueries.insert(
    availability: Availabilities,
    bookingAvailability: BookingAvailability,
    attendeeId: User.Id,
): Meeting.Id = transactionWithResult {
    insert(
        availabilityId = availability.id,
        attendees = setOf(attendeeId,  availability.ownerId),
        startInstant = bookingAvailability.startInstant,
        durationMinutes = availability.durationMinutes,
    )
    Meeting.Id(lastInsertId().executeAsOne())
}

fun MeetingsQueries.getByUserIdFlow(id: User.Id) =
    getByUserId(setOf(id).adaptedIntersectionRegex { it.value.toString() })
        .asFlow()
        .mapToList()

fun Meetings.toMeetings() = Meeting(
    id = id,
    startInstant = startInstant,
    durationMinutes = durationMinutes,
    attendees = attendees,
)