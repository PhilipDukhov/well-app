package com.well.sharedMobileTest


enum class TestScreen {
    Local,

    Welcome,
    Call,
    MyProfile,
    Slider,
    UserChat,
    UserRating,
    AvailabilityCalendar,
    Calendar,
    GradientView,
    DoubleCalendar,
    Favorites,
    Donate,
    ExpertsFilter,
    ;

    companion object {
        val initial = MyProfile
        val allCases = values().toList()
    }
}