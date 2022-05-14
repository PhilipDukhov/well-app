package com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar

import com.well.modules.models.Availability
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.LocalTime
import com.well.modules.models.date.dateTime.atTime
import com.well.modules.models.date.dateTime.minus
import com.well.modules.models.date.dateTime.now
import com.well.modules.models.date.dateTime.plus
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.viewUtils.GlobalStringsBase
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

object CreateAvailabilityFeature {
    object Strings : GlobalStringsBase() {
        const val start = "Starts"
        const val end = "Ends"
        const val repeat = "Repeat"
        const val newAvailability = "New Availability"
        const val editAvailability = "Edit Availability"
    }

    fun initialStateCreate(startDate: LocalDate) =
        State(
            availability = Availability(
                id = Availability.Id(-1),
                startDay = startDate,
                startTime = LocalTime.now() + 1.hours,
                durationMinutes = 30,
                repeat = Repeat.None,
            ),
            type = State.Type.Creating,
        )

    fun initialStateUpdate(availability: Availability) =
        State(
            availability = availability,
            type = State.Type.Editing,
        )

    data class State(
        val availability: Availability,
        val type: Type,
    ) {
        enum class Type {
            Creating,
            Editing,
            ;
        }

        val title = when (type) {
            Type.Creating -> Strings.newAvailability
            Type.Editing -> Strings.editAvailability
        }
        val finishButtonTitle = when (type) {
            Type.Creating -> Strings.add
            Type.Editing -> Strings.done
        }
        val startTimeValid = availability.startInstant >= Clock.System.now()
        val endTimeValid = availability.durationMinutes > 0
        val valid = (availability.id.value >= 0 || startTimeValid) && endTimeValid

        fun copy(startTime: LocalTime) = copyAvailability {
            copy(
                startInstant = startDay
                    .atTime(startTime)
                    .toInstant(TimeZone.currentSystemDefault()),

                )
        }

        fun copyAvailability(block: Availability.() -> Availability) =
            copy(availability = availability.run(block))
    }

    sealed class Msg {
        class SetStartTime(val time: LocalTime) : Msg()
        class SetEndTime(val time: LocalTime) : Msg()
        class SetRepeat(val repeat: Repeat) : Msg()
        object Save : Msg()
        object Delete : Msg()
    }

    sealed interface Eff {
        class Save(val availability: Availability) : Eff
        object Delete : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.SetStartTime -> {
                    return@state state.copy(startTime = msg.time)
                }
                is Msg.SetEndTime -> {
                    val oneDay = 1.days
                    var duration = (msg.time - state.availability.startTime)
                    if (!duration.isPositive() && duration.absoluteValue > oneDay / 2) {
                        duration += oneDay
                    }
                    return@state state.copyAvailability {
                        copy(durationMinutes = duration.inWholeMinutes.toInt())
                    }
                }
                is Msg.SetRepeat -> {
                    return@state state.copyAvailability {
                        copy(repeat = msg.repeat)
                    }
                }
                Msg.Save -> {
                    return@eff Eff.Save(state.availability)
                }
                Msg.Delete -> {
                    return@eff Eff.Delete
                }
            }
        })
    }.withEmptySet()
}