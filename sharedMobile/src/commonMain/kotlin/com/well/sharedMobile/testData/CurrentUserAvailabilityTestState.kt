package com.well.sharedMobile.testData

import com.well.modules.models.Availability
import com.well.modules.models.Repeat
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.CurrentUserAvailabilityFeature
import io.ktor.util.date.*
import kotlin.time.Duration

private data class TestState(
    val daysOffset: Int,
    val repeat: Repeat,
    val startTime: Int,
    val endTime: Int
) {
    fun availability(i: Int, time: GMTDate) =
        (time + Duration.days(daysOffset)).copy(hours = startTime).let { startTime ->
            Availability(
                id = i,
                startTime = startTime,
                endTime = startTime.copy(hours = endTime),
                repeat = repeat,
            )
        }

}

// CurrentUserAvailabilityFeature unused for easy readability
@Suppress("unused")
fun CurrentUserAvailabilityFeature.testState() =
    GMTDate().let { today ->
        CurrentUserAvailabilityFeature.State(
            availabilities = listOf(
                TestState(
                    daysOffset = 0,
                    repeat = Repeat.Weekly,
                    startTime = 12,
                    endTime = 13
                ),
                TestState(
                    daysOffset = 2,
                    repeat = Repeat.Weekends,
                    startTime = 10,
                    endTime = 11
                ),
                TestState(
                    daysOffset = 3,
                    repeat = Repeat.None,
                    startTime = 9,
                    endTime = 12
                ),
            ).mapIndexed { i, state ->
                state.availability(i, today)
            }
        )
    }