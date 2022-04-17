package com.well.modules.models

import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Meeting(
    val id: Id,
    override val startInstant: Instant,
    override val durationMinutes: Int,
    val expertUid: User.Id,
    val creatorUid: User.Id,
    val state: State,
) : AvailabilityInfo {
    @Serializable
    @JvmInline
    value class Id(val value: Long) {
        override fun toString() = value.toString()

        object ColumnAdapter : com.squareup.sqldelight.ColumnAdapter<Id, Long> {
            override fun decode(databaseValue: Long): Id =
                Id(databaseValue)

            override fun encode(value: Id): Long =
                value.value
        }
    }

    @Serializable
    sealed class State {
        @Serializable
        object Requested : State()

        @Serializable
        object Confirmed : State()

        @Serializable
        data class Rejected(val reason: String) : State()

        @Serializable
        data class Canceled(val reason: String) : State()
    }

    val dateTimeDescription = "on ${startDay.localizedDayAndShortMonth()} at $startTime"

    fun otherUid(currentUid: User.Id) =
        if (expertUid == currentUid)
            creatorUid
        else
            expertUid
}