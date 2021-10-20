package com.well.modules.models.date.dateTime

import kotlinx.datetime.*


val LocalDateTime.time: LocalTime
	get() = LocalTime(hour = hour, minute = minute)

// https://kotlinlang.slack.com/archives/C01923PC6A0/p1597788327006500
fun LocalDateTime.toTimestamp(timeZone: TimeZone): Timestamp =
	toInstant(timeZone)

fun LocalDateTime.Companion.parseOrNull(isoString: String): LocalDateTime? =
	runCatching { parse(isoString) }.getOrNull()
