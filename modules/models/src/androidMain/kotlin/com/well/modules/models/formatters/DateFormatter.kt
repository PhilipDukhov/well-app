package com.well.modules.models.formatters

import com.well.modules.models.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val timeFormatter = SimpleDateFormat.getTimeInstance(DateFormat.SHORT)

actual fun DateFormatter.Companion.format(
    date: Date,
    timeZoneIdentifier: String
): String {
    val timeZone = TimeZone.getTimeZone(timeZoneIdentifier)
    val timeZoneDisplayName = timeZone.getDisplayName(false, TimeZone.SHORT)
    timeFormatter.timeZone = timeZone
    return "$timeZoneDisplayName ${timeFormatter.format(date.date)}"
}

actual fun timeZonesIdentifiersList(): List<String> =
    TimeZone.getAvailableIDs().toList()

actual fun currentTimeZoneIdentifier(): String =
    TimeZone.getDefault().id