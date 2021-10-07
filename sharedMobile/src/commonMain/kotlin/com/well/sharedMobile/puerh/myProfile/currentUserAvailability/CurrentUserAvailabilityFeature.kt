package com.well.sharedMobile.puerh.myProfile.currentUserAvailability

import com.well.modules.models.Availability
import com.well.modules.models.AvailabilityId
import com.well.modules.models.Repeat
import com.well.modules.models.weekend
import com.well.modules.models.workday
import com.well.modules.utils.date.firstDayOfWeek
import com.well.modules.utils.positiveRem
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.soywiz.klock.DateTime
import io.github.aakira.napier.Napier
import io.ktor.util.date.*
import kotlin.time.Duration

object CurrentUserAvailabilityFeature {
    data class State(
        internal val availabilities: List<Availability> = listOf(),
        val monthOffset: Int = 0,
    ) {
        data class CalendarItem(
            val date: GMTDate,
            val isCurrentMonth: Boolean,
            val isCurrentDay: Boolean,
            val availabilities: List<Availability>,
        )

        val month: Month
        val year: Int?
        val allDaysOfWeek = WeekDay.values().toList()
        val days: List<List<CalendarItem>>
        private val today = GMTDate()
        val allAvailabilities: List<Availability>

        init {
            val now = DateTime.now()
            now.month

            val currentMonthFirstDay =
                today.monthOffset(dayOfMonth = 1, monthOffset = monthOffset)
            month = currentMonthFirstDay.month
            year = currentMonthFirstDay.year.let { year ->
                if (year != today.year) year else null
            }
            val nextMonth = currentMonthFirstDay.monthOffset(dayOfMonth = 1, monthOffset = 1).month
            val days = mutableListOf<List<CalendarItem>>()
            val firstDayOfWeek = WeekDay.firstDayOfWeek
            val weekDaysCount = WeekDay.values().count()
            val daysBeforeFirstWeekDay =
                ((firstDayOfWeek.ordinal - currentMonthFirstDay.dayOfWeek.ordinal) - weekDaysCount) % weekDaysCount
            var loopDay = currentMonthFirstDay + Duration.days(daysBeforeFirstWeekDay)
            val allAvailabilities = mutableListOf<Availability>()
            while (loopDay.month != nextMonth) {
                days += (0 until weekDaysCount)
                    .map { loopDay + Duration.days(it) }
                    .map { day ->
                        val availabilities = if (day.yearAndDay < today.yearAndDay)
                            listOf()
                        else
                            availabilities
                                .filter { availability ->
                                    if (day.yearAndDay < availability.startTime.yearAndDay) {
                                        return@filter false
                                    }
                                    if (day.yearAndDay == availability.startTime.yearAndDay) {
                                        return@filter true
                                    }
                                    when (val repeat = availability.repeat) {
                                        Repeat.None -> {
                                            false
                                        }
                                        is Repeat.WeekDays -> {
                                            repeat.weekDays.contains(day.dayOfWeek)
                                        }
                                        Repeat.Weekends -> {
                                            day.dayOfWeek.weekend
                                        }
                                        Repeat.Workdays -> {
                                            day.dayOfWeek.workday
                                        }
                                        Repeat.Weekly -> {
                                            availability.startTime.dayOfWeek == day.dayOfWeek
                                        }
                                    }
                                }
                                .map {
                                    it.copy(startTime = it.startTime)
                                }
                        allAvailabilities.addAll(availabilities)
                        CalendarItem(
                            date = day,
                            isCurrentMonth = day.month == month,
                            isCurrentDay = day.yearAndDay == today.yearAndDay,
                            availabilities = availabilities
                        )
                    }
                loopDay += Duration.days(weekDaysCount)
            }
            this.days = days.toList()
            this.allAvailabilities = allAvailabilities
        }
    }

    sealed class Msg {
        object PrevMonth : Msg()
        object NextMonth : Msg()
        data class Add(val availability: Availability) : Msg()
        data class Remove(val availabilityId: AvailabilityId) : Msg()
    }

    sealed class Eff {
        data class Add(val availability: Availability) : Eff()
        data class Remove(val availabilityId: AvailabilityId) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.Add -> {
                    return@reducer state.copy(
                        availabilities = state.availabilities + msg.availability
                    ) toSetOf Eff.Add(msg.availability)
                }
                is Msg.Remove -> {
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
            }
        })
    }.withEmptySet()
}

val GMTDate.yearAndDay: String
    get() = "$year|$dayOfYear"

fun GMTDate.monthOffset(dayOfMonth: Int, monthOffset: Int): GMTDate {
    val monthValue = month.ordinal + monthOffset
    val monthCount = Month.values().count()
    val month = Month.from(monthValue.positiveRem(monthCount))
    val year = year + (monthValue / monthCount).let {
        if (monthValue < 0)
            it - 1
        else
            it
    }
    return GMTDate(dayOfMonth = dayOfMonth, month = month, year = year)
}

fun GMTDate(dayOfMonth: Int, month: Month, year: Int) =
    GMTDate(
        seconds = 0,
        minutes = 0,
        hours = 0,
        dayOfMonth = dayOfMonth,
        month = month,
        year = year
    )