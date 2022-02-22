package com.well.modules.models.formatters

import com.well.modules.models.date.Date
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
actual fun DateFormatter.Companion.formatChatMessage(date: Date): String {
    timeFormatter.timeZone = TimeZone.getDefault()
    return timeFormatter.format(date.date)
}