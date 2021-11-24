package com.well.modules.db.meetings

import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.utils.dbUtils.InstantColumnAdapter
import com.well.modules.utils.flowUtils.mapIterable
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun MeetingsQueries.listFlow() =
    list().asFlow().mapToList()

fun MeetingsQueries.listIdsFlow() =
    listIds().asFlow().mapToList()

fun MeetingsQueries.insertAll(meetings: List<Meeting>) =
    transaction {
        meetings.forEach { meeting ->
            insert(
                id = meeting.id,
                hostId = meeting.hostId,
                attendeeId = meeting.attendeeId,
                startInstant = meeting.startInstant,
                durationMinutes = meeting.durationMinutes,
            )
        }
    }

fun MeetingsQueries.removeAll(ids: List<Meeting.Id>) =
    transaction {
        ids.forEach { id ->
            delete(id)
        }
    }

fun MeetingsQueries.getByIdsFlow(ids: Collection<Meeting.Id>) =
    getByIds(ids)
        .asFlow()
        .mapToList()
        .mapIterable(Meetings::toMeetings)

fun Meetings.toMeetings() = Meeting(
    id = id,
    startInstant = startInstant,
    durationMinutes = durationMinutes,
    hostId = hostId,
    attendeeId = attendeeId,
)

fun MeetingsDatabase.Companion.create(driver: SqlDriver) =
    MeetingsDatabase(
        driver = driver, MeetingsAdapter = Meetings.Adapter(
            idAdapter = Meeting.Id.ColumnAdapter,
            hostIdAdapter = User.Id.ColumnAdapter,
            attendeeIdAdapter = User.Id.ColumnAdapter,
            startInstantAdapter = InstantColumnAdapter,
        )
    )
