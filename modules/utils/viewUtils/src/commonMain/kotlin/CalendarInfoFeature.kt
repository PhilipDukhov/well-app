package com.well.modules.utils.viewUtils

import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.firstDayOfWeek
import com.well.modules.models.date.dateTime.monthOffset
import com.well.modules.models.date.dateTime.today
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus


object CalendarInfoFeature {
    data class State<Event>(
        val monthOffset: Int,
        val selectedDate: LocalDate?,
        val prepareDayEvents: (LocalDate) -> List<Event>,
        val hasBadge: (List<Event>) -> Boolean,
    ) {
        companion object {
            val allDaysOfWeek = DayOfWeek.values().toList()
        }

        data class Item<Event>(
            val date: LocalDate,
            val isCurrentMonth: Boolean,
            val isCurrentDay: Boolean,
            val hasBadge: Boolean,
            val events: List<Event>,
        )

        val days: List<List<Item<Event>>>
        val monthEvents: List<Event>
        val today = LocalDate.today()
        fun monthFirstDay(monthOffset: Int) = today
            .monthOffset(dayOfMonth = 1, monthOffset = monthOffset)

        val currentMonthFirstDay = monthFirstDay(monthOffset)
        val month: Month = currentMonthFirstDay.month

        init {
            val nextMonth = currentMonthFirstDay.monthOffset(dayOfMonth = 1, monthOffset = 1).month
            val days = mutableListOf<List<Item<Event>>>()
            val firstDayOfWeek = firstDayOfWeek
            val weekDaysCount = DayOfWeek.values().count()
            val daysBeforeFirstWeekDay =
                ((firstDayOfWeek.ordinal - currentMonthFirstDay.dayOfWeek.ordinal) - weekDaysCount) % weekDaysCount
            var loopDay = currentMonthFirstDay.daysShift(daysBeforeFirstWeekDay)
            val monthEvents = mutableListOf<Event>()
            while (loopDay.month != nextMonth) {
                days += (0 until weekDaysCount)
                    .map { loopDay.daysShift(it) }
                    .map { day ->
                        val events = prepareDayEvents(day)
                        monthEvents.addAll(events)
                        Item(
                            date = day,
                            isCurrentMonth = day.month == month,
                            isCurrentDay = day == today,
                            events = events,
                            hasBadge = hasBadge(events)
                        )
                    }
                loopDay = loopDay.plus(DateTimeUnit.DayBased(weekDaysCount))
            }
            this.days = days.toList()
            this.monthEvents = monthEvents
        }

        val selectedItem = days.firstNotNullOfOrNull { weekDays ->
            weekDays.firstOrNull { it.date == selectedDate }
        }
    }

    sealed class Msg {
        object PrevMonth : Msg()
        object NextMonth : Msg()
        data class SelectDate(val selectedDate: LocalDate) : Msg()
    }

    fun <Event> reduceMsg(
        msg: Msg,
        state: State<Event>,
    ): State<Event> = when (msg) {
        Msg.PrevMonth -> {
            state.copy(
                monthOffset = state.monthOffset - 1
            )
        }
        Msg.NextMonth -> {
            state.copy(
                monthOffset = state.monthOffset + 1
            )
        }
        is Msg.SelectDate -> {
            if (msg.selectedDate == state.selectedDate)
                state.copy(
                    selectedDate = null
                )
            else {
                state.copy(
                    selectedDate = msg.selectedDate,
                    monthOffset = state.monthOffset + when {
                        msg.selectedDate.month == state.month -> 0
                        msg.selectedDate < state.currentMonthFirstDay -> -1
                        else -> 1
                    }
                )
            }
        }
    }
}