package com.well.modules.db.server

import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun MeetingsQueries.insert(availability: Availabilities, attendeeId: User.Id): Meeting.Id =
    transactionWithResult {
        insert(
            availabilityId = availability.id,
            hostId = availability.ownerId,
            attendeeId = attendeeId,
            startInstant = availability.startInstant,
            durationMinutes = availability.durationMinutes,
        )
        Meeting.Id(lastInsertId().executeAsOne())
    }

fun MeetingsQueries.getByUserIdFlow(id: User.Id) =
    getByUserId(id)
        .asFlow()
        .mapToList()

fun Meetings.toMeetings() = Meeting(
    id = id,
    startInstant = startInstant,
    durationMinutes = durationMinutes,
    hostId = hostId,
    attendeeId = attendeeId,
)