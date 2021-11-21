package com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar

import com.well.modules.models.Availability
import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.firstDayOfWeek
import com.well.modules.models.date.dateTime.localizedName
import com.well.modules.models.date.dateTime.monthOffset
import com.well.modules.models.date.dateTime.today
import com.well.modules.models.mapDayAvailabilities
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.GlobalStringsBase
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus

object AvailabilitiesCalendarFeature {
    object Strings : GlobalStringsBase()

    data class State(
        internal val availabilities: List<Availability>? = null,
        val failureReason: String? = null,
        val processing: Boolean = false,
        val monthOffset: Int = 0,
    ) {
        companion object {
            val allDaysOfWeek = DayOfWeek.values().toList()
            const val availabilityCellsCount = 3
        }

        data class CalendarItem(
            val date: LocalDate,
            val isCurrentMonth: Boolean,
            val isCurrentDay: Boolean,
            val availabilities: List<Availability>,
            val canCreateAvailability: Boolean,
        )
        val loaded = availabilities != null
        val month: Month
        val weeks: List<List<CalendarItem>>
        val monthAvailabilities: List<Availability>
        val calendarTitle: String

        init {
            val today = LocalDate.today()
            val currentMonthFirstDay = today
                .monthOffset(dayOfMonth = 1, monthOffset = monthOffset)
            month = currentMonthFirstDay.month
            val year = currentMonthFirstDay.year.let { year ->
                if (year != today.year) year else null
            }
            calendarTitle = month.localizedName + (year?.let { ", $it" } ?: "")
            val nextMonth = currentMonthFirstDay.monthOffset(dayOfMonth = 1, monthOffset = 1).month
            val days = mutableListOf<List<CalendarItem>>()
            val firstDayOfWeek = firstDayOfWeek
            val weekDaysCount = DayOfWeek.values().count()
            val daysBeforeFirstWeekDay =
                ((firstDayOfWeek.ordinal - currentMonthFirstDay.dayOfWeek.ordinal) - weekDaysCount) % weekDaysCount
            var loopDay = currentMonthFirstDay.daysShift(daysBeforeFirstWeekDay)
            val monthAvailabilities = mutableListOf<Availability>()
            while (loopDay.month != nextMonth) {
                days += (0 until weekDaysCount)
                    .map { loopDay.daysShift(it) }
                    .map { day ->
                        val availabilities = availabilities
                            ?.let { availabilities.mapDayAvailabilities(day = day) }
                            ?: emptyList()
                        monthAvailabilities.addAll(availabilities)
                        CalendarItem(
                            date = day,
                            isCurrentMonth = day.month == month,
                            isCurrentDay = day == today,
                            availabilities = availabilities,
                            canCreateAvailability = day >= today,
                        )
                    }
                loopDay = loopDay.plus(DateTimeUnit.DayBased(weekDaysCount))
            }
            this.weeks = days.toList()
            this.monthAvailabilities = monthAvailabilities
        }
    }

    sealed class Msg {
        object PrevMonth : Msg()
        object NextMonth : Msg()
        data class Add(val availability: Availability) : Msg()
        data class Update(val availability: Availability) : Msg()
        data class Delete(val availabilityId: Availability.Id) : Msg()
        object ReloadAvailabilities : Msg()
        data class SetAvailabilities(val availabilities: List<Availability>) : Msg()
        data class RequestFailed(val reason: String) : Msg()
        data class SetProcessing(val processing: Boolean) : Msg()
    }

    sealed interface Eff {
        object RequestAvailabilities : Eff
        data class Add(val availability: Availability) : Eff
        data class Remove(val availabilityId: Availability.Id) : Eff
        data class Update(val availability: Availability) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.ReloadAvailabilities -> {
                    return@reducer state.copy(
                        failureReason = null,
                    ) toSetOf Eff.RequestAvailabilities
                }
                is Msg.SetAvailabilities -> {
                    return@state state.copy(availabilities = msg.availabilities)
                }
                is Msg.Add -> {
                    return@eff Eff.Add(msg.availability)
                }
                is Msg.Delete -> {
                    return@eff Eff.Remove(msg.availabilityId)
                }
                Msg.NextMonth -> {
                    return@state state.copy(monthOffset = state.monthOffset + 1)
                }
                Msg.PrevMonth -> {
                    return@state state.copy(monthOffset = state.monthOffset - 1)
                }
                is Msg.Update -> {
                    return@eff Eff.Update(msg.availability)
                }
                is Msg.RequestFailed -> {
                    return@state state.copy(failureReason = msg.reason)
                }
                is Msg.SetProcessing -> {
                    return@state state.copy(processing = msg.processing)
                }
            }
        })
    }.withEmptySet()
}