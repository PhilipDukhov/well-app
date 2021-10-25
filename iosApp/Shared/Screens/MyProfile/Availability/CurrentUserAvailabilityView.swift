//
//  CurrentUserAvailabilityView.swift
//  Well
//
//  Created by Phil on 22.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = CurrentUserAvailabilitiesListFeature

struct CurrentUserAvailabilityView: View {
    let state: CurrentUserAvailabilitiesListFeature.State
    let listener: (CurrentUserAvailabilitiesListFeature.Msg) -> Void

    enum PresentingDialog: Identifiable {
        case create(LocalDate)
        case update(Availability)

        var id: UInt {
            switch self {
            case .create(let date): return date.hash()
            case .update(let availability): return UInt(availability.id)
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
            ).style(.init(fontSize: 15, fontWeight: .bold))
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
            ).sheet(item: $presentingDialog) { _ in
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
                        .foregroundColorKMM(ColorConstants.LightBlue)
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
                return ColorConstants.White
            } else {
                return ColorConstants.DarkGrey
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
                    Rectangle().foregroundColorKMM(ColorConstants.DarkGrey)
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
                .style(.h4.copy(fontWeight: .light))
            Button(action: showNextMonth) {
                Image(systemName: "arrow.forward")
            }
        }.font(.system(size: 20)).foregroundColor(.black)
    }
}

private struct AvailabilitiesList: View {
    var selectedItem: Feature.StateCalendarItem?

    let allAvailabilities: [Availability]
    let onSelect: (Availability) -> Void
    let onCreate: (Feature.StateCalendarItem) -> Void

    var body: some View {
        EmptyView()
    }
}

//@Composable
//private fun AvailabilityCell(
//    firstRowText: String?,
//    secondRowText: String,
//    aspectRatio: Float?,
//    modifier: Modifier = Modifier,
//    onClick: () -> Unit = {},
//) {
//    AvailabilityCell(
//        onClick = onClick,
//        aspectRatio = aspectRatio,
//        modifier = modifier,
//    ) {
//        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//            firstRowText?.let {
//                AutoSizeText(it)
//            }
//            AutoSizeText(secondRowText)
//        }
//    }
//}
