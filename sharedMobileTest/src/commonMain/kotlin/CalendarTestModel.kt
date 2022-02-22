package com.well.sharedMobileTest

import com.well.modules.features.calendar.calendarFeature.CalendarFeature
import com.well.modules.models.MeetingViewModel


val CalendarFeature.State.Companion.testState
    get() = CalendarFeature.reducer(
        msg = CalendarFeature.Msg.UpdateMeetings(MeetingViewModel.testValues(30)),
        state = CalendarFeature.State()
    ).first
