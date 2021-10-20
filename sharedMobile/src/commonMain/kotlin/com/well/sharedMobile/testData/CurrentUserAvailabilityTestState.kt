package com.well.sharedMobile.testData

import com.well.modules.models.Availability
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.time
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

private data class TestState(
    val daysOffset: Int,
    val repeat: Repeat,
    val startTime: Int,
    val hoursDuration: Int
) {
    fun availability(i: Int) =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let { now ->
            Availability(
                id = i,
                startDay = now.date.daysShift(i),
                startTime = now.time,
                durationMinutes = Duration.hours(hoursDuration).inWholeMinutes.toInt(),
                repeat = repeat,
            )
        }
}

// CurrentUserAvailabilitiesListFeature unused for easy readability
@Suppress("unused")
fun CurrentUserAvailabilitiesListFeature.testState() =
    CurrentUserAvailabilitiesListFeature.State(
        availabilities = listOf(
            TestState(
                daysOffset = 0,
                repeat = Repeat.Weekly,
                startTime = 12,
                hoursDuration = 1
            ),
            TestState(
                daysOffset = 2,
                repeat = Repeat.Weekends,
                startTime = 10,
                hoursDuration = 1
            ),
            TestState(
                daysOffset = 3,
                repeat = Repeat.None,
                startTime = 9,
                hoursDuration = 3
            ),
        ).mapIndexed { i, state ->
            state.availability(i)
        }
    )
