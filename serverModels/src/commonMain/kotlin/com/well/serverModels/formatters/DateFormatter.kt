package com.well.serverModels.formatters

import com.well.serverModels.Date

class DateFormatter {
    companion object
}

expect fun DateFormatter.Companion.format(date: Date, timeZoneIdentifier: String): String

expect fun timeZonesIdentifiersList(): List<String>

expect fun currentTimeZoneIdentifier(): String