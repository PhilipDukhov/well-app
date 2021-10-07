@file:UseSerializers(WeekDayAsIntSerializer::class)

package com.well.modules.models

import com.well.modules.models.serializers.WeekDayAsIntSerializer
import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
sealed class Repeat {
    companion object {
        fun Repeat(weekDays: Set<WeekDay>): Repeat =
            when (weekDays) {
                WeekDay.workdays -> Workdays
                WeekDay.weekends -> Weekends
                else -> WeekDays(weekDays)
            }
    }

    @Serializable
    object None : Repeat()

    @Serializable
    object Weekly : Repeat()

    @Serializable
    object Workdays : Repeat()

    @Serializable
    object Weekends : Repeat()

    @Serializable
    data class WeekDays(
        val weekDays: Set<WeekDay>,
    ) : Repeat()
}

val WeekDay.weekend: Boolean
    get() = when (this) {
        WeekDay.SATURDAY, WeekDay.SUNDAY -> true
        else -> false
    }

val WeekDay.workday: Boolean
    get() = !weekend

val WeekDay.Companion.workdays: Set<WeekDay>
    get() = WeekDay.values().filter { it.workday }.toSet()

val WeekDay.Companion.weekends: Set<WeekDay>
    get() = WeekDay.values().filter { it.weekend }.toSet()

typealias AvailabilityId = Int

data class Availability(
    val id: AvailabilityId,
    val startTime: GMTDate,
    val endTime: GMTDate,
    val repeat: Repeat,
)
