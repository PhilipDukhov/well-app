package com.well.modules.features.calendar.calendarFeature

import com.well.modules.models.MeetingViewModel
import com.well.modules.models.User
import com.well.modules.models.date.dateTime.localizedName
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.CalendarInfoFeature
import com.well.modules.utils.viewUtils.GlobalStringsBase
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

object CalendarFeature {
    object Strings : GlobalStringsBase() {
        const val now = "Now"
    }

    data class State(
        internal val meetings: List<MeetingViewModel> = emptyList(),
        val infoState: CalendarInfoFeature.State<MeetingViewModel> = CalendarInfoFeature.State(
            monthOffset = 0,
            selectedDate = null,
            prepareDayEvents = prepareDayEvents(meetings),
        ),
    ) {
        val currentMonthName = infoState.month.localizedName
        val prevMonthName = infoState.monthFirstDay(infoState.monthOffset - 1).month.localizedName
        val nextMonthName = infoState.monthFirstDay(infoState.monthOffset + 1).month.localizedName
        val year = infoState.currentMonthFirstDay.year
        val upcomingMeetings = meetings
            .filter { it.endInstant > Clock.System.now() }
            .groupBy { it.startDay }
            .entries
            .sortedBy { it.key }
            .map { mapEntry ->
                DayMeetings(
                    day = mapEntry.key,
                    meetings = mapEntry.value.sortedBy { it.startTime },
                )
            }
        val selectedItemMeetings = infoState.selectedItem?.let {
            DayMeetings(day = it.date, meetings = it.events)
        }

        data class DayMeetings(
            val day: LocalDate,
            val meetings: List<MeetingViewModel>,
        )

        companion object {
            fun prepareDayEvents(meetings: List<MeetingViewModel>) =
                { day: LocalDate ->
                    meetings.filter { it.startDay == day }
                }
        }

        fun copy(meetings: List<MeetingViewModel>) =
            copy(
                meetings = meetings,
                infoState = infoState.copy(
                    prepareDayEvents = prepareDayEvents(meetings)
                )
            )
    }

    sealed class Msg {
        data class UpdateMeetings(val meetings: List<MeetingViewModel>) : Msg()
        data class OpenUserProfile(val meeting: MeetingViewModel) : Msg()
        data class StartCall(val meeting: MeetingViewModel) : Msg()

        internal data class CalendarInfoMsg(val msg: CalendarInfoFeature.Msg) : Msg()

        companion object {
            val PrevMonth: Msg = CalendarInfoMsg(CalendarInfoFeature.Msg.PrevMonth)
            val NextMonth: Msg = CalendarInfoMsg(CalendarInfoFeature.Msg.NextMonth)

            @Suppress("FunctionName")
            fun SelectDate(selectedDate: LocalDate): Msg =
                CalendarInfoMsg(CalendarInfoFeature.Msg.SelectDate(selectedDate))
        }
    }

    sealed interface Eff {
        data class OpenUserProfile(val uid: User.Id) : Eff
        data class StartCall(val uid: User.Id) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.UpdateMeetings -> {
                    return@state state.copy(
                        meetings = msg.meetings,
                    )
                }
                is Msg.CalendarInfoMsg -> {
                    return@state state.copy(
                        infoState = CalendarInfoFeature.reduceMsg(msg.msg, state.infoState)
                    )
                }
                is Msg.OpenUserProfile -> {
                    val uid = msg.meeting.user?.id ?: return@state state
                    return@eff Eff.OpenUserProfile(uid)
                }
                is Msg.StartCall -> {
                    val uid = msg.meeting.user?.id ?: return@state state
                    return@eff Eff.StartCall(uid)
                }
            }
        })
    }.withEmptySet()
}