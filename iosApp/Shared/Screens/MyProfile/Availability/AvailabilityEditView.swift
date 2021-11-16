//
//  AvailabilityEditView.swift
//  Well
//
//  Created by Phil on 27.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

private typealias Feature = CreateAvailabilityFeature

struct AvailabilityEditView: View {
    private let initialState: Feature.State
    private let onCancel: () -> Void
    private let onFinish: (Availability) -> Void
    
    static func createNew(
        startDay: LocalDate,
        onCancel: @escaping () -> Void,
        onFinish: @escaping (Availability) -> Void
    ) -> Self {
        .init(
            initialState: Feature().initialStateCreate(startDate: startDay),
            onCancel: onCancel,
            onFinish: onFinish
        )
    }
    
    static func update(
        availability: Availability,
        onCancel: @escaping () -> Void,
        onFinish: @escaping (Availability) -> Void
    ) -> Self {
        .init(
            initialState: Feature().initialStateUpdate(availability: availability),
            onCancel: onCancel,
            onFinish: onFinish
        )
    }
    
    private init(
        initialState: Feature.State,
        onCancel: @escaping () -> Void,
        onFinish: @escaping (Availability) -> Void
    ) {
        self.initialState = initialState
        self.onCancel = onCancel
        self.onFinish = onFinish
    }
    
    var body: some View {
        ReducerView(
            initial: initialState,
            reducer: Feature().reducer,
            view: {
                Content(state: $0, listener: $1, onCancel: onCancel)
            },
            effHandler: { (eff: CreateAvailabilityFeatureEff) in
                switch eff {
                case let eff as CreateAvailabilityFeatureEffSave:
                    onFinish(eff.availability)
                    
                default: break
                }
            }
        ).foregroundColorKMM(.companion.DarkGrey)
            .animation(.none)
    }
}

private struct Content: View {
    let state: Feature.State
    let listener: (Feature.Msg) -> Void
    let onCancel: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            NavigationBar(
                title: state.title,
                leftItem: .init(
                    text: Feature.Strings.shared.cancel,
                    handler: onCancel
                ),
                rightItem: .init(
                    view: Text(state.finishButtonTitle),
                    enabled: state.valid,
                    handler: {
                        listener(Feature.MsgSave())
                    }
                )
            )
            content
        }
    }
    
    var content: some View {
        List {
            Section {
                DatePicker(
                    Feature.Strings.shared.start,
                    selection: state.availability.startTime
                        .toDateBinding(
                            listener: listener,
                            createMsg: Feature.MsgSetStartTime.init
                        ),
                    displayedComponents: .hourAndMinute
                )
                DatePicker(
                    Feature.Strings.shared.end,
                    selection: state.availability.endTime
                        .toDateBinding(
                            listener: listener,
                            createMsg: Feature.MsgSetEndTime.init
                        ),
                    displayedComponents: .hourAndMinute
                )
                Menu(
                    content: {
                        ForEach(Repeat.companion.allCases, id: \.self) { value in
                            Toggle(
                                value.title.lowercased(),
                                isOn: .init(
                                    get: { value == state.availability.repeat },
                                    set: {
                                        if $0 {
                                            listener(Feature.MsgSetRepeat(repeat: value))
                                        }
                                    }
                                )
                            )
                        }
                    },
                    label: {
                        HStack {
                            Text(Feature.Strings().repeat)
                            Spacer().fillMaxWidth()
                            Text(state.availability.repeat.title)
                            Image(systemName: "chevron.right")
                        }.foregroundColorKMM(.companion.DarkGrey)
                    }
                )
            }
            if state.type == .editing {
                Section {
                    HStack {
                        Spacer()
                        Button(Feature.Strings.shared.delete_) {
                            listener(Feature.MsgDelete())
                        }.foregroundColorKMM(.companion.RadicalRed)
                        Spacer()
                    }
                }
            }
        }.listStyle(.insetGrouped)
    }
}

private extension LocalTime {
    func toDateBinding<Msg>(
        listener: @escaping (Msg) -> Void,
        createMsg: @escaping (LocalTime) -> Msg
    ) -> Binding<Foundation.Date> {
        let date = Calendar.current.date(
            from: todayInstant()
                .toLocalDateTime(timeZone: .companion.currentSystemDefault())
                .toNSDateComponents()
        )!
        return Binding {
            date
        } set: { date in
            let localTime = ConvertersKt.toKotlinInstant(date).toLocalTime(timeZone: .companion.currentSystemDefault())
            listener(createMsg(localTime))
        }
    }
}
