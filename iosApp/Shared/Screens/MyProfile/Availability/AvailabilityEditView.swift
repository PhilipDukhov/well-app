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
            view: { state, listener in
                Content(
                    state: state,
                    listener: listener
                ).navigationBarTitleDisplayMode(.inline)
                    .navigationTitle(state.title)
                    .navigationBarItems(
                        leading: Button("Cancel", action: onCancel),
                        trailing: Button(state.finishButtonTitle) {
                            listener(Feature.MsgSave())
                        }.disabled(!state.valid)
                    )
            },
            effHandler: { (eff: Feature.Eff) in
                switch eff {
                case let eff as Feature.EffSave:
                    onFinish(eff.availability)
                    
                default: break
                }
            }
        )
    }
}

private struct Content: View {
    let state: Feature.State
    let listener: (Feature.Msg) -> Void
    
    var body: some View {
        Form {
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
                Picker(
                    Feature.Strings().repeat,
                    selection: .init(get: { state.availability.repeat }, set: { listener(Feature.MsgSetRepeat(repeat: $0)) })
                ) {
                    ForEach(Repeat.companion.allCases, id: \.self) {
                        Text($0.name)
                    }
                }
            }
            if state.type == .editing {
                Section {
                    Button(Feature.Strings.shared.delete_) {
                        listener(Feature.MsgDelete())
                    }.foregroundColorKMM(.companion.RadicalRed)
                }
            }
        }.listStyle(.grouped)
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
