package com.well.modules.models.date.dateTime

import kotlinx.datetime.*


// TimeZone.Companion.of() should rename parameter 'zoneId' to 'id' as the 'zone' is redundant.
public fun TimeZone.Companion.ofOrNull(id: String): TimeZone? =
	runCatching { of(id) }.getOrNull()
