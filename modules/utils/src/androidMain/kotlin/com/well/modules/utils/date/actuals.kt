package com.well.modules.utils.date

import io.ktor.util.date.*
import java.text.DateFormatSymbols
import java.util.*

actual val WeekDay.Companion.firstDayOfWeek: WeekDay
    get() = from((Calendar.getInstance().firstDayOfWeek + weekDaysCount - Calendar.MONDAY) % weekDaysCount)

actual val WeekDay.localizedVeryShortSymbol: String
    get() = DateFormatSymbols.getInstance().shortWeekdays[ordinal + 1].first().toString()

actual val Month.localizedName: String
    get() = DateFormatSymbols.getInstance().months[ordinal]


actual val Month.localizedShortName: String
    get() = DateFormatSymbols.getInstance().shortMonths[ordinal]

private val weekDaysCount = WeekDay.values().count()