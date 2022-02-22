package com.well.modules.utils.viewUtils

import com.well.modules.models.Color

data class CalendarMonthViewColors(
    val dayOfWeekColor: Color,
    val dayColors: DayColors,
) {
    data class DayColors(
        val nonSelectedContentColor: Color,
        val selectedContentColor: Color,
        val selectedBackgroundColor: Color,
        val currentDayBackgroundIsGradient: Boolean,
    )

    companion object {
        val availabilitiesCalendar = CalendarMonthViewColors(
            dayOfWeekColor = Color.LightBlue,
            dayColors = DayColors(
                nonSelectedContentColor = Color.DarkGrey,
                selectedContentColor = Color.White,
                selectedBackgroundColor = Color.DarkGrey,
                currentDayBackgroundIsGradient = true,
            ),
        )

        val mainCalendar = CalendarMonthViewColors(
            dayOfWeekColor = Color.White.copy(alpha = 0.65f),
            dayColors = DayColors(
                nonSelectedContentColor = Color.White,
                selectedContentColor = Color.MainGradientAvg,
                selectedBackgroundColor = Color.White,
                currentDayBackgroundIsGradient = false,
            ),
        )
    }
}
