package com.well.modules.models.date.dateTime

import kotlin.time.*
import kotlinx.datetime.*


// https://kotlinlang.slack.com/archives/C01923PC6A0/p1597788327006500
typealias Timestamp = Instant

@ExperimentalTime
fun Timestamp.durationSince(other: Timestamp): Duration =
	this - other


@ExperimentalTime
fun Timestamp.durationUntil(other: Timestamp): Duration =
	other - this


fun Instant.Companion.parseOrNull(isoString: String): Timestamp? =
	runCatching { parse(isoString) }.getOrNull()


fun Timestamp.toLocalDate(timeZone: TimeZone): LocalDate =
	toLocalDateTime(timeZone).date


fun Timestamp.toLocalTime(timeZone: TimeZone): LocalTime =
	toLocalDateTime(timeZone).time
