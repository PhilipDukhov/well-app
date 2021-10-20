package com.well.modules.models.date.dateTime

import java.util.*
import kotlinx.datetime.*

fun Timestamp.toJavaDate(): Date =
	Date.from(toJavaInstant())
