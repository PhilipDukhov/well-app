package com.well.modules.models.date.dateTime

import kotlinx.datetime.DayOfWeek
import java.text.DateFormatSymbols
import java.util.*

actual val firstDayOfWeek: DayOfWeek
    get() = DayOfWeek.values()[(Calendar.getInstance().firstDayOfWeek + weekDaysCount - Calendar.MONDAY) % weekDaysCount]

actual val DayOfWeek.localizedVeryShortSymbol: String
    get() = DateFormatSymbols.getInstance().shortWeekdays[ordinal + 1].first().toString()

actual val DayOfWeek.localizedSymbol
    get() = DateFormatSymbols.getInstance().weekdays[ordinal + 1]

private val weekDaysCount = DayOfWeek.values().count()