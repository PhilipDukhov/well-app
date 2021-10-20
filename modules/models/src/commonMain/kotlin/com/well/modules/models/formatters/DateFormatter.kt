package com.well.modules.models.formatters

import com.well.modules.models.date.Date

class DateFormatter {
    companion object
}

expect fun DateFormatter.Companion.format(date: Date, timeZoneIdentifier: String): String
expect fun DateFormatter.Companion.formatChatMessage(date: Date): String
