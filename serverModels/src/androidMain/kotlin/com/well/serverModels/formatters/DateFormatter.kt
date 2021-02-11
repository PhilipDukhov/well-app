package com.well.serverModels.formatters

import com.well.serverModels.Date
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
    return "$timeZoneDisplayName ${timeFormatter.format(date)}"
}