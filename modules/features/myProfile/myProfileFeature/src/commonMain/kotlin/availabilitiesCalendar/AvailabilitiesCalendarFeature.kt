package com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar

import com.well.modules.models.Availability
import com.well.modules.models.date.dateTime.localizedName
import com.well.modules.models.mapDayAvailabilities
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.CalendarInfoFeature
import com.well.modules.utils.viewUtils.GlobalStringsBase
import kotlinx.datetime.LocalDate

object AvailabilitiesCalendarFeature {
    object Strings : GlobalStringsBase()

    data class State(
        internal val availabilities: List<Availability>? = null,
        val failureReason: String? = null,
        val processing: Boolean = false,
        val infoState: CalendarInfoFeature.State<Availability> = CalendarInfoFeature.State(
            monthOffset = 0,
            selectedDate = null,
            prepareDayEvents = { day ->
                availabilities
                    ?.let { availabilities.mapDayAvailabilities(day) }
                    ?: emptyList()
            },
            hasBadge = { false },
        ),
    ) {
        companion object {
            const val availabilityCellsCount = 3

            fun prepareDayEvents(availabilities: List<Availability>?) =
                { day: LocalDate ->
                    availabilities
                        ?.let { availabilities.mapDayAvailabilities(day) }
                        ?: emptyList()
                }
        }

        val calendarTitle: String

        init {
            val monthYear = infoState.currentMonthFirstDay.year
            val yearIfNeeded = if (monthYear != infoState.today.year) ", $monthYear" else ""
            calendarTitle = infoState.month.localizedName + yearIfNeeded
        }

        val loaded = availabilities != null

        fun canCreateAvailability(item: CalendarInfoFeature.State.Item<Availability>) =
            item.date >= infoState.today

        fun copy(availabilities: List<Availability>?) =
            copy(
                availabilities = availabilities,
                infoState = infoState.copy(
                    prepareDayEvents = prepareDayEvents(availabilities)
                )
            )
    }

    sealed class Msg {
        internal class CalendarInfoMsg(val msg: CalendarInfoFeature.Msg) : Msg()

        companion object {
            val PrevMonth: Msg = CalendarInfoMsg(CalendarInfoFeature.Msg.PrevMonth)
            val NextMonth: Msg = CalendarInfoMsg(CalendarInfoFeature.Msg.NextMonth)

            @Suppress("FunctionName")
            fun SelectDate(selectedDate: LocalDate): Msg =
                CalendarInfoMsg(CalendarInfoFeature.Msg.SelectDate(selectedDate))
        }

        class Add(val availability: Availability) : Msg()
        class Update(val availability: Availability) : Msg()
        class Delete(val availabilityId: Availability.Id) : Msg()
        object ReloadAvailabilities : Msg()
        class SetAvailabilities(val availabilities: List<Availability>) : Msg()
        class RequestFailed(val reason: String) : Msg()
        class SetProcessing(val processing: Boolean) : Msg()
    }

    sealed interface Eff {
        object RequestAvailabilities : Eff
        class Add(val availability: Availability) : Eff
        class Remove(val availabilityId: Availability.Id) : Eff
        class Update(val availability: Availability) : Eff
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
                is Msg.Update -> {
                    return@eff Eff.Update(msg.availability)
                }
                is Msg.RequestFailed -> {
                    return@state state.copy(failureReason = msg.reason)
                }
                is Msg.SetProcessing -> {
                    return@state state.copy(processing = msg.processing)
                }
                is Msg.CalendarInfoMsg -> {
                    return@state state.copy(
                        infoState = CalendarInfoFeature.reduceMsg(msg.msg, state.infoState)
                    )
                }
            }
        })
    }.withEmptySet()
}