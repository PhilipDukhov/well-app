package com.well.modules.models.formatters

import com.well.modules.models.Date

class DateFormatter {
    companion object
}

expect fun DateFormatter.Companion.format(date: Date, timeZoneIdentifier: String): String
