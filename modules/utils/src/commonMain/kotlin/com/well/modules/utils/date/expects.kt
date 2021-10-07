package com.well.modules.utils.date

import com.well.modules.models.Availability
import io.ktor.util.date.*

expect val WeekDay.Companion.firstDayOfWeek: WeekDay
expect val Month.localizedName: String
expect val Month.localizedShortName: String
expect val WeekDay.localizedVeryShortSymbol: String

val GMTDate.localizedDayAndShortMonth: String
    get() = "$dayOfMonth ${month.localizedShortName}"

val GMTDate.hoursAndMinutes: String
    get() = "$hours:${minutes}"

val Availability.intervalString: String
    get() = "${startTime.hoursAndMinutes}-${endTime.hoursAndMinutes}"
