package com.well.modules.features.myProfile.myProfileFeature.currentUserAvailability

import com.well.modules.models.Availability
import com.well.modules.models.Repeat
import com.well.modules.models.date.dateTime.today
import com.well.modules.models.date.dateTime.weekend
import com.well.modules.models.date.dateTime.workday
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

object AvailabilitiesConverter {
    fun mapDayAvailabilities(day: LocalDate, minInterval: Duration = Duration.ZERO, availabilities: List<Availability>): List<Availability> {
        val now = Clock.System.now()
        return if (day < LocalDate.today())
            listOf()
        else
            availabilities
                .filter { availability ->
                    if (day < availability.startDay) {
                        return@filter false
                    }
                    if (day == availability.startDay) {
                        return@filter true
                    }
                    when (availability.repeat) {
                        Repeat.None -> {
                            false
                        }
                        Repeat.Weekends -> {
                            day.dayOfWeek.weekend
                        }
                        Repeat.Workdays -> {
                            day.dayOfWeek.workday
                        }
                        Repeat.Weekly -> {
                            availability.startDay.dayOfWeek == day.dayOfWeek
                        }
                    }
                }
                .map {
                    it.copy(startDay = day)
                }
                .filter { it.startInstant + minInterval > now }
    }
}