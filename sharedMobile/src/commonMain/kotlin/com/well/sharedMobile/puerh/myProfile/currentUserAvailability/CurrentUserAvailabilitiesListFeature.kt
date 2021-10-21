package com.well.sharedMobile.puerh.myProfile.currentUserAvailability

import com.well.modules.models.Availability
import com.well.modules.models.AvailabilityId
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.firstDayOfWeek
import com.well.modules.models.date.dateTime.monthOffset
import com.well.modules.models.date.dateTime.toLocalDate
import com.well.modules.models.date.dateTime.today
import com.well.modules.models.date.dateTime.weekend
import com.well.modules.models.date.dateTime.workday
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.puerh.πModels.GlobalStringsBase
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

object CurrentUserAvailabilitiesListFeature {
    object Strings : GlobalStringsBase() {
        const val newAvailability = "New Availability"
        const val updateAvailability = "Update Availability"
    }

    data class State(
        internal val availabilities: List<Availability> = listOf(),
        val monthOffset: Int = 0,
    ) {
        companion object {
            val allDaysOfWeek = DayOfWeek.values().toList()
        }

        data class CalendarItem(
            val date: LocalDate,
            val isCurrentMonth: Boolean,
            val isCurrentDay: Boolean,
            val availabilities: List<Availability>,
            val canCreateAvailability: Boolean,
        )

        val month: Month
        val year: Int?
        val days: List<List<CalendarItem>>
        val monthAvailabilities: List<Availability>

        init {
            val today = LocalDate.today()
            val currentMonthFirstDay = today
                .monthOffset(dayOfMonth = 1, monthOffset = monthOffset)
            month = currentMonthFirstDay.month
            year = currentMonthFirstDay.year.let { year ->
                if (year != today.year) year else null
            }
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
                        val availabilities = AvailabilitiesConverter.mapDayAvailabilities(
                            day = day,
                            availabilities = availabilities,
                        )
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
            this.days = days.toList()
            this.monthAvailabilities = monthAvailabilities
        }
    }

    sealed class Msg {
        object PrevMonth : Msg()
        object NextMonth : Msg()
        data class Add(val availability: Availability) : Msg()
        data class Update(val availability: Availability) : Msg()
        data class Delete(val availabilityId: AvailabilityId) : Msg()
        data class SetAvailabilities(val availabilities: List<Availability>) : Msg()
    }

    sealed class Eff {
        data class Add(val availability: Availability) : Eff()
        data class Remove(val availabilityId: AvailabilityId) : Eff()
        data class Update(val availability: Availability) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        when (msg) {
            is Msg.SetAvailabilities -> {
                return@state state.copy(availabilities = msg.availabilities)
            }
            is Msg.Add -> {
                return@reducer state.copy(
                    availabilities = state.availabilities + msg.availability
                ) toSetOf Eff.Add(msg.availability)
            }
            is Msg.Delete -> {
                return@reducer state.copy(
                    availabilities = state.availabilities.filter { it.id != msg.availabilityId }
                ) toSetOf Eff.Remove(msg.availabilityId)
            }
            Msg.NextMonth -> {
                return@state state.copy(monthOffset = state.monthOffset + 1)
            }
            Msg.PrevMonth -> {
                return@state state.copy(monthOffset = state.monthOffset - 1)
            }
            is Msg.Update -> {
                return@reducer state.copy(
                    availabilities = state.availabilities.map { availability ->
                        if (availability.id == msg.availability.id) {
                            msg.availability
                        } else {
                            availability
                        }
                    }
                ) toSetOf Eff.Update(msg.availability)
            }
        }
    }.withEmptySet()
}
