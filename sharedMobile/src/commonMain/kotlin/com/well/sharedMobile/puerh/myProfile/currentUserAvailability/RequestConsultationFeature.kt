package com.well.sharedMobile.puerh.myProfile.currentUserAvailability

import com.well.modules.models.Availability
import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.today
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.puerh.πModels.GlobalStringsBase
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

object RequestConsultationFeature {
    object Strings : GlobalStringsBase() {
        const val title = "Book day and time"
        const val bookNow = "Book now"
    }

    fun initial() = State(State.Status.Loading) toSetOf Eff.Update

    data class State(
        val status: Status,
        val availabilitiesByDay: List<Pair<LocalDate, List<Availability>>> = listOf(),
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
        data class UpdateAvailabilities(val availabilities: List<Availability>) : Msg()
        data class Book(val availability: Availability) : Msg()
        object Booked : Msg()
        object Close : Msg()
        object ClearFailedState : Msg()
        data class BookingFailed(
            val reason: String,
            val newAvailabilities: List<Availability>? = null
        ) : Msg()
    }

    sealed class Eff {
        object Update : Eff()
        data class Book(val availability: Availability) : Eff()
        data class Close(val timeoutMillis: Long = 0) : Eff()
        data class ClearFailedState(val timeoutMillis: Long = 2000) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.Close -> {
                    return@eff Eff.Close()
                }
                Msg.ClearFailedState -> {
                    return@state state.copy(status = State.Status.Loaded)
                }
                is Msg.UpdateAvailabilities -> {
                    return@state state.copy(
                        status = State.Status.Loaded,
                        availabilitiesByDay = convertAvailabilities(msg.availabilities),
                    )
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
                        availabilitiesByDay = msg.newAvailabilities?.let { newAvailabilities ->
                            convertAvailabilities(newAvailabilities)
                        } ?: state.availabilitiesByDay
                    ) toSetOf Eff.ClearFailedState()
                }
            }
        })
    }.withEmptySet()

    private fun convertAvailabilities(availabilities: List<Availability>) =
        LocalDate.today().let { today ->
            List(30) {
                today.daysShift(it)
            }.map { day ->
                day to AvailabilitiesConverter.mapDayAvailabilities(
                    day,
                    minInterval = Duration.hours(1),
                    availabilities
                )
            }.filter { it.second.isNotEmpty() }
        }
}