package com.well.modules.models.date.dateTime

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus


fun LocalDate.atTime(time: LocalTime): LocalDateTime =
    time.atDate(this)

fun LocalDate.Companion.parseOrNull(isoString: String): LocalDate? =
    runCatching { parse(isoString) }.getOrNull()

fun LocalDate.monthOffset(dayOfMonth: Int, monthOffset: Int): LocalDate {
    val monthValue = month.ordinal + monthOffset
    val monthCount = Month.values().count()
    val month = Month.values()[monthValue.positiveRem(monthCount)]
    val year = year + (monthValue / monthCount).let {
        if (monthValue < 0)
            it - 1
        else
            it
    }
    return LocalDate(dayOfMonth = dayOfMonth, month = month, year = year)
}

fun LocalDate.localizedDayAndShortMonth(separator: String = " ") = "$dayOfMonth$separator${month.localizedShortName}"

fun LocalDate.daysShift(days: Int): LocalDate = when {
    days < 0 -> {
        minus(DateTimeUnit.DayBased(-days))
    }
    days > 0 -> {
        plus(DateTimeUnit.DayBased(days))
    }
    else -> this
}

fun LocalDate.Companion.today() = Clock.System.now().toLocalDate(TimeZone.currentSystemDefault())

private fun Int.positiveRem(other: Int): Int {
    var rem = rem(other)
    if (rem < 0) {
        rem += other
    }
    return rem
}
