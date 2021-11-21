package com.well.modules.models

import kotlinx.datetime.LocalDate

typealias BookingAvailabilitiesListByDay = List<Pair<LocalDate, List<BookingAvailability>>>