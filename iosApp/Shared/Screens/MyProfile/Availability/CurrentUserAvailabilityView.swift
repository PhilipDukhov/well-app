//
//  CurrentUserAvailabilityView.swift
//  Well
//
//  Created by Phil on 22.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = AvailabilitiesCalendarFeature

struct CurrentUserAvailabilityView: View {
    let state: AvailabilitiesCalendarFeature.State
    let listener: (AvailabilitiesCalendarFeature.Msg) -> Void

    enum PresentingDialog: Identifiable {
        case create(LocalDate)
        case update(Availability)

        var id: String {
            switch self {
            case .create(let date): return date.toString()
            case .update(let availability): return availability.id.toString()
            }
        }
    }

    @State
    private var presentingDialog: PresentingDialog?

    @State
    private var selectedDate: LocalDate?

    var body: some View {
        VStack(spacing: 0) {
            CalendarView(
                state: state,
                selectedDate: $selectedDate,
                showNextMonth: {
                    listener(Feature.MsgNextMonth())
                },
                showPrevMonth: {
                    listener(Feature.MsgPrevMonth())
                }
            ).textStyle(.init(fontSize: 15, fontWeight: .bold))
            AvailabilitiesList(
                selectedItem: state.weeks.firstNotNullOfOrNull {
                    $0.first {
                        $0.date == selectedDate
                    }
                },
                allAvailabilities: state.monthAvailabilities,
                onSelect: {
                    presentingDialog = .update($0)
                },
                onCreate: {
                    presentingDialog = .create($0.date)
                }
            ).sheet(item: $presentingDialog) { dialog in
                let finish = { (msg: Feature.Msg?) in
                    presentingDialog = nil
                    msg.map(listener)
                }
                switch dialog {
                case .create(let date):
                    AvailabilityEditView.createNew(
                        startDay: date,
                        onCancel: {
                            finish(nil)
                        },
                        onFinish: { availability in
                            finish(Feature.MsgAdd(availability: availability))
                        }
                    )
                    
                case .update(let availability):
                    AvailabilityEditView.update(
                        availability: availability,
                        onCancel: {
                            finish(nil)
                        },
                        onFinish: { availability in
                            finish(Feature.MsgUpdate(availability: availability))
                        }
                    )
                }
            }
            Spacer()
        }.padding(.horizontal)
    }
}

private struct CalendarView: View {
    let state: Feature.State

    @Binding
    var selectedDate: LocalDate?

    let showNextMonth: () -> Void
    let showPrevMonth: () -> Void

    var body: some View {
        CalendarTitleView(
            title: state.calendarTitle,
            showNextMonth: showNextMonth,
            showPrevMonth: showPrevMonth
        )
        Spacer().frame(height: 24)
        Group {
            HStack(spacing: 0) {
                ForEach(Feature.StateCompanion().allDaysOfWeek) { weekDay in
                    Text(weekDay.localizedVeryShortSymbol)
                        .foregroundColorKMM(.companion.LightBlue)
                        .fillMaxWidth()
                }
            }
            ForEachIndexed(state.weeks) { _, week in
                HStack(spacing: 0) {
                    ForEach(week, id: \.date) { day in
                        DayView(day: day, selectedDate: $selectedDate)
                    }
                }.padding(.vertical, 3)
            }
        }.swipeableLeftRight(
            onLeft: showNextMonth,
            onRight: showPrevMonth
        )
    }
}

private struct DayView: View {
    let day: Feature.StateCalendarItem

    @Binding
    var selectedDate: LocalDate?

    let selected: Bool
    let textColor: SwiftUI.Color

    init(day: Feature.StateCalendarItem, selectedDate: Binding<LocalDate?>) {
        self.day = day
        _selectedDate = selectedDate
        let selected = day.date == selectedDate.wrappedValue
        self.selected = selected
        textColor = { () -> SharedMobile.Color in
            if day.isCurrentDay || selected {
                return .companion.White
            } else {
                return .companion.DarkGrey
            }
        }().toColor().opacity(day.isCurrentMonth ? 1 : 0.5)
    }

    var body: some View {
        ZStack {
            ZStack {
                if day.isCurrentDay {
                    GradientView(gradient: .main)
                        .fillMaxSize()
                        .opacity(selected ? 1 : 0.5)
                } else if selected {
                    Rectangle().foregroundColorKMM(.companion.DarkGrey)
                        .opacity(day.isCurrentMonth ? 1 : 0.5)
                }
                VStack(spacing: 0) {
                    Spacer().fillMaxHeight()
                    Text(day.date.dayOfMonth.toString())
                        .foregroundColor(textColor)
                    ZStack(alignment: .top) {
                        Rectangle().foregroundColor(.clear)
                        if day.availabilities.isNotEmpty {
                            Circle()
                                .foregroundColor(textColor)
                                .frame(size: 2)
                        }
                    }
                }
            }.frame(size: 35)
                .clipShape(Circle())
                .onTapGesture {
                    selectedDate = selectedDate == day.date ? nil : day.date
                }
        }.fillMaxWidth()
    }
}

private struct CalendarTitleView: View {
    let title: String

    let showNextMonth: () -> Void
    let showPrevMonth: () -> Void

    var body: some View {
        HStack {
            Button(action: showPrevMonth) {
                Image(systemName: "arrow.backward")
            }
            Text(title)
                .textStyle(.h4.copy(fontWeight: .light))
            Button(action: showNextMonth) {
                Image(systemName: "arrow.forward")
            }
        }.font(.system(size: 20)).foregroundColor(.black)
    }
}
