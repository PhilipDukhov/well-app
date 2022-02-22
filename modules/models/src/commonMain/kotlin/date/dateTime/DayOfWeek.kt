package com.well.modules.models.date.dateTime

import kotlinx.datetime.DayOfWeek

val DayOfWeek.weekend
    get() = when (this) {
        DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> true
        else -> false
    }

val DayOfWeek.workday get() = !weekend

expect val firstDayOfWeek: DayOfWeek
expect val DayOfWeek.localizedVeryShortSymbol: String
expect val DayOfWeek.localizedSymbol: String