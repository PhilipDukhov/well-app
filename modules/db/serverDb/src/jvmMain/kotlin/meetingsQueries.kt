package com.well.modules.db.server

import com.well.modules.models.BookingAvailability
import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun MeetingsQueries.insert(
    availability: Availabilities,
    bookingAvailability: BookingAvailability,
    attendeeId: User.Id,
): Meeting.Id = transactionWithResult {
    insert(
        availabilityId = availability.id,
        expertUid  = availability.ownerId,
        creatorUid = attendeeId,
        state = Meeting.State.Requested,
        startInstant = bookingAvailability.startInstant,
        durationMinutes = availability.durationMinutes,
    )
    Meeting.Id(lastInsertId().executeAsOne())
}

fun MeetingsQueries.getByUserIdFlow(id: User.Id) =
    getByUserId(id)
        .asFlow()
        .mapToList()

fun Meetings.toMeeting() = Meeting(
    id = id,
    startInstant = startInstant,
    durationMinutes = durationMinutes,
    expertUid  = expertUid,
    creatorUid = creatorUid,
    state = state,
)