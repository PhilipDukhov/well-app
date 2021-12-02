//
//  CalendarMonthView.swift
//  Well
//
//  Created by Phil on 01.12.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct CalendarMonthView<Event: NSObject, TitleView: View>: View {
    let state: CalendarInfoFeatureState<Event>

    @ViewBuilder
    let title: () -> TitleView

    let colors: CalendarMonthViewColors
    let onSelect: (CalendarInfoFeatureStateItem<Event>) -> Void

    let showNextMonth: () -> Void
    let showPrevMonth: () -> Void

    var body: some View {
        title()
        Spacer().frame(height: 24)
        Group {
            HStack(spacing: 0) {
                ForEach(CalendarInfoFeatureState<Event>.companion.allDaysOfWeek) { weekDay in
                    Text(weekDay.localizedVeryShortSymbol)
                        .foregroundColorKMM(colors.dayOfWeekColor)
                        .fillMaxWidth()
                }
            }
            ForEachIndexed(state.days) { _, week in
                HStack(spacing: 0) {
                    ForEach(week, id: \.date) { day in
                        DayView(
                            day: day,
                            selectedItem: state.selectedDate,
                            colors: colors.dayColors
                        ) {
                            onSelect(day)
                        }
                    }
                }.padding(.vertical, 3)
            }
        }.contentShape(Rectangle()).swipeableLeftRight(
            onLeft: showNextMonth,
            onRight: showPrevMonth
        )
            .textStyle(.init(fontSize: 15, fontWeight: .bold))
    }
}

private struct DayView<Event: NSObject>: View {
    let day: CalendarInfoFeatureStateItem<Event>

    let selectedItem: LocalDate?
    let onSelect: () -> Void
    let colors: CalendarMonthViewColors.DayColors

    let selected: Bool
    let textColor: SwiftUI.Color

    init(
        day: CalendarInfoFeatureStateItem<Event>,
        selectedItem: LocalDate?,
        colors: CalendarMonthViewColors.DayColors,
        onSelect: @escaping () -> Void
    ) {
        self.day = day
        self.selectedItem = selectedItem
        self.onSelect = onSelect
        self.colors = colors
        let selected = day.date == selectedItem
        self.selected = selected
        textColor = { () -> SharedMobile.Color in
            if day.isCurrentDay && colors.currentDayBackgroundIsGradient {
                return .companion.White
            }
            if selected {
                return colors.selectedContentColor
            } else {
                return colors.nonSelectedContentColor
            }
        }().toColor().opacity(day.isCurrentMonth ? 1 : 0.5)
    }

    var body: some View {
        ZStack {
            ZStack {
                if day.isCurrentDay && colors.currentDayBackgroundIsGradient {
                    GradientView(gradient: .main)
                        .fillMaxSize()
                        .opacity(selected ? 1 : 0.5)
                } else if day.isCurrentDay || selected {
                    Rectangle().foregroundColorKMM(colors.selectedBackgroundColor)
                        .opacity(day.isCurrentDay && !selected ? 0.3 : 1)
                }
                VStack(spacing: 0) {
                    Spacer().fillMaxHeight()
                    Text(day.date.dayOfMonth.toString())
                        .foregroundColor(textColor)
                    ZStack(alignment: .top) {
                        Rectangle().foregroundColor(.clear)
                        if day.events.isNotEmpty {
                            Circle()
                                .foregroundColor(textColor)
                                .frame(size: 2)
                        }
                    }
                }
            }.frame(size: 35)
                .clipShape(Circle())
                .onTapGesture(perform: onSelect)
        }.fillMaxWidth()
    }
}
