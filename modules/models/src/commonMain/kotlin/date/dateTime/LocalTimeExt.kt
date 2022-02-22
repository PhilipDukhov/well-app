package com.well.modules.models.date.dateTime

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Duration

fun LocalTime.atDate(date: LocalDate): LocalDateTime =
    LocalDateTime(
        year = date.year,
        monthNumber = date.monthNumber,
        dayOfMonth = date.dayOfMonth,
        hour = hour,
        minute = minute,
    )


val LocalTime.Companion.midnight: LocalTime
    get() = min

fun LocalTime.Companion.now() = Clock.System.now().toLocalTime(TimeZone.currentSystemDefault())

fun LocalTime.todayInstant() = TimeZone.currentSystemDefault().let { timeZone ->
    val today = Clock.System.now().toLocalDate(timeZone)
    atDate(today).toInstant(timeZone)
}

operator fun LocalTime.minus(time: LocalTime): Duration {
    return todayInstant() - time.todayInstant()
}

operator fun LocalTime.plus(duration: Duration): LocalTime {
    return (todayInstant() + duration).toLocalTime(TimeZone.currentSystemDefault())
}