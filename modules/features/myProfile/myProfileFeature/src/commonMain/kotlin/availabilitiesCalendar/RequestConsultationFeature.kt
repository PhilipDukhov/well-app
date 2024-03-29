package com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar

import com.well.modules.models.BookingAvailabilitiesListByDay
import com.well.modules.models.BookingAvailability
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.GlobalStringsBase

object RequestConsultationFeature {
    object Strings : GlobalStringsBase() {
        const val title = "Book day and time"
        const val bookNow = "Book now"
        const val bookingFailed = "Booking failed:"
        const val hasNoConsultations = "User has no available consultations"
    }

    fun initial() = State(State.Status.Loading) toSetOf Eff.Update

    data class State(
        val status: Status,
        val availabilitiesByDay: BookingAvailabilitiesListByDay = listOf(),
    ) {
        sealed class Status {
            object Loading : Status()
            object Loaded : Status()
            object Processing : Status()
            object Booked : Status()
            data class BookingFailed(val reason: String) : Status()
        }
    }

    sealed class Msg {
        class UpdateAvailabilitiesByDay(val availabilitiesByDay: BookingAvailabilitiesListByDay) : Msg()
        class Book(val availability: BookingAvailability) : Msg()
        object Booked : Msg()
        object Close : Msg()
        object ClearFailedState : Msg()
        data class BookingFailed(
            val reason: String,
            val newAvailabilities: BookingAvailabilitiesListByDay? = null,
        ) : Msg()
    }

    sealed interface Eff {
        object Update : Eff
        class Book(val availability: BookingAvailability) : Eff
        class Close(val timeoutMillis: Long = 0) : Eff
        class ClearFailedState(val timeoutMillis: Long = 2000) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.Close -> {
                    return@eff Eff.Close()
                }
                Msg.ClearFailedState -> {
                    return@state state.copy(status = State.Status.Loaded)
                }
                is Msg.UpdateAvailabilitiesByDay -> {
                    return@reducer state.copy(
                        status = State.Status.Loaded,
                        availabilitiesByDay = msg.availabilitiesByDay,
                    ) toSetOf if (msg.availabilitiesByDay.isEmpty()) Eff.Close(1500) else null
                }
                is Msg.Book -> {
                    return@reducer state.copy(
                        status = State.Status.Processing,
                    ) toSetOf Eff.Book(msg.availability)
                }
                Msg.Booked -> {
                    return@reducer state.copy(
                        status = State.Status.Booked,
                    ) toSetOf Eff.Close(500)
                }
                is Msg.BookingFailed -> {
                    return@reducer state.copy(
                        status = State.Status.BookingFailed(msg.reason),
                        availabilitiesByDay = msg.newAvailabilities ?: state.availabilitiesByDay
                    ) toSetOf Eff.ClearFailedState()
                }
            }
        })
    }.withEmptySet()
}