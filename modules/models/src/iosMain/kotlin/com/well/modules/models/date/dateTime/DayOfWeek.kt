package com.well.modules.models.date.dateTime

import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar

actual val firstDayOfWeek
    get() = DayOfWeek.values()[(NSCalendar.currentCalendar.firstWeekday.toInt() + weekDaysCount - 2) % weekDaysCount]

actual val DayOfWeek.localizedVeryShortSymbol
    get() = NSCalendar.currentCalendar.veryShortWeekdaySymbols[(ordinal + 1) % weekDaysCount] as String

actual val DayOfWeek.localizedSymbol
    get() = NSCalendar.currentCalendar.weekdaySymbols[(ordinal + 1) % weekDaysCount] as String


private val weekDaysCount = DayOfWeek.values().count()