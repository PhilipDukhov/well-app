package com.well.sharedMobile.puerh.myProfile.currentUserAvailability

import com.well.modules.models.Availability
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.LocalTime
import com.well.modules.models.date.dateTime.minus
import com.well.modules.models.date.dateTime.now
import com.well.modules.models.date.dateTime.plus
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.puerh.πModels.GlobalStringsBase
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

object CreateAvailabilityFeature {
    object Strings : GlobalStringsBase() {
        const val start = "Start"
        const val end = "End"
        const val repeat = "Repeat"
    }

    fun initialState(startDate: LocalDate) =
        Availability(
            id = -1,
            startDay = startDate,
            startTime = LocalTime.now() + Duration.minutes(30),
            durationMinutes = 30,
            repeat = Repeat.None,
        )

    sealed class Msg {
        data class SetStartTime(val time: LocalTime) : Msg()
        data class SetEndTime(val time: LocalTime) : Msg()
        data class SetRepeat(val repeat: Repeat) : Msg()
        object Save : Msg()
    }

    sealed class Eff {
        data class Save(val availability: Availability) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: Availability
    ): Pair<Availability, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.SetStartTime -> {
                    return@state state.copy(startTime = msg.time)
                }
                is Msg.SetEndTime -> {
                    val oneDay = Duration.days(1)
                    var duration = (msg.time - state.startTime)
                    if (!duration.isPositive() && duration.absoluteValue > oneDay / 2) {
                        duration += oneDay
                    }
                    return@state state.copy(
                        durationMinutes = duration.inWholeMinutes.toInt()
                    )
                }
                is Msg.SetRepeat -> {
                    return@state state.copy(repeat = msg.repeat)
                }
                Msg.Save -> {
                    return@eff Eff.Save(state)
                }
            }
        })
    }.withEmptySet()
}