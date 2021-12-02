//
//  AvailabilitiesCalendarView.swift
//  Well
//
//  Created by Phil on 22.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = AvailabilitiesCalendarFeature

struct AvailabilitiesCalendarView: View {
    let state: AvailabilitiesCalendarFeature.State
    let listener: (AvailabilitiesCalendarFeature.Msg) -> Void

    private enum PresentingDialog: Identifiable {
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
        ZStack {
            content
            if !state.loaded || state.processing {
                InactiveOverlay {
                    if let failureReason = state.failureReason {
                        VStack {
                            Text(failureReason)
                                .textStyle(.body2)
                                .foregroundColorKMM(.companion.White)
                            Button(action: { listener(Feature.MsgReloadAvailabilities()) }) {
                                Text(Feature.Strings.shared.tryAgain)
                                    .textStyle(.subtitle1)
                            }
                        }
                    } else {
                        ProgressView()
                            .onAppear {
                                if !state.loaded {
                                    listener(Feature.MsgReloadAvailabilities())
                                }
                            }
                    }
                }
            }
        }.fillMaxSize()
    }

    var content: some View {
        VStack(spacing: 0) {
            CalendarMonthView(
                state: state.infoState,
                title: {
                    CalendarTitleView(title: state.calendarTitle, showNextMonth: showNextMonth, showPrevMonth: showPrevMonth)
                },
                colors: CalendarMonthViewColors.companion.availabilitiesCalendar,
                onSelect: {
                    listener(Feature.MsgCompanion.shared.SelectDate(selectedDate: $0.date))
                },
                showNextMonth: showNextMonth,
                showPrevMonth: showPrevMonth
            )
            AvailabilitiesList(
                selectedItem: state.infoState.selectedItem,
                allAvailabilities: state.infoState.monthEvents as! [Availability],
                onSelect: {
                    presentingDialog = .update($0)
                },
                onCreate: {
                    presentingDialog = .create($0.date)
                },
                canCreateAvailability: state.canCreateAvailability
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

    func showNextMonth() {
        listener(Feature.MsgCompanion.shared.NextMonth)
    }

    func showPrevMonth() {
        listener(Feature.MsgCompanion.shared.PrevMonth)
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
