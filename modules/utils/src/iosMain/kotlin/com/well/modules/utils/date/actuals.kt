package com.well.modules.utils.date

import io.ktor.util.date.*
import platform.Foundation.NSCalendar

actual val WeekDay.Companion.firstDayOfWeek: WeekDay
    get() = from((NSCalendar.currentCalendar.firstWeekday.toInt() + weekDaysCount - 2) % weekDaysCount)

actual val WeekDay.localizedVeryShortSymbol: String
    get() = NSCalendar.currentCalendar.veryShortWeekdaySymbols[(ordinal + 1) % weekDaysCount] as String


actual val Month.localizedShortName: String
    get() = NSCalendar.currentCalendar.shortMonthSymbols[ordinal] as String

actual val Month.localizedName: String
    get() = NSCalendar.currentCalendar.monthSymbols[ordinal] as String

private val weekDaysCount = WeekDay.values().count()