package com.well.modules.db.meetings

import com.well.modules.models.Meeting
import com.well.modules.models.User
import com.well.modules.utils.dbUtils.InstantColumnAdapter
import com.well.modules.utils.flowUtils.mapIterable
import com.squareup.sqldelight.EnumColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList

fun MeetingsQueries.listFlow() =
    list()
        .asFlow()
        .mapToList()
        .mapIterable(Meetings::toMeetings)

fun MeetingsQueries.listIdAndStatesFlow() =
    listIdAndStates().asFlow().mapToList()

fun MeetingsQueries.insertAll(meetings: List<Meeting>) =
    transaction {
        meetings.forEach { meeting ->
            with(meeting) {
                insert(
                    id = id,
                    expertUid = expertUid,
                    creatorUid = creatorUid,
                    state = state,
                    startInstant = startInstant,
                    durationMinutes = durationMinutes,
                )
            }
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
    expertUid = expertUid,
    creatorUid = creatorUid,
    state = state,
)

fun MeetingsDatabase.Companion.create(driver: SqlDriver) =
    MeetingsDatabase(
        driver = driver, MeetingsAdapter = Meetings.Adapter(
            idAdapter = Meeting.Id.ColumnAdapter,
            startInstantAdapter = InstantColumnAdapter,
            expertUidAdapter = User.Id.ColumnAdapter,
            creatorUidAdapter = User.Id.ColumnAdapter,
            stateAdapter = EnumColumnAdapter(),
        )
    )
