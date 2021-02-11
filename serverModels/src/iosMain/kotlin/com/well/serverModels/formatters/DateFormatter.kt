package com.well.serverModels.formatters

import com.well.serverModels.Date
import platform.Foundation.*

private val timeZoneFormatter = NSDateFormatter().apply {
    dateFormat = "O"
}
private val timeFormatter = NSDateFormatter().apply {
    dateStyle = NSDateFormatterNoStyle
    timeStyle = NSDateFormatterShortStyle
}

actual fun DateFormatter.Companion.format(date: Date, timeZoneIdentifier: String): String {
    timeZoneFormatter.timeZone = NSTimeZone.timeZoneWithName(timeZoneIdentifier) ?: NSTimeZone.defaultTimeZone
    timeFormatter.timeZone = timeZoneFormatter.timeZone
    return "${timeZoneFormatter.stringFromDate(date.date)} ${timeFormatter.stringFromDate(date.date)}"
}