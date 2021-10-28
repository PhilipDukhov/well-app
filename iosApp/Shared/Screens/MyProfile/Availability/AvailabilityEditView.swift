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
            effHandler: { (eff: Feature.Eff) in
                switch eff {
                case let eff as Feature.EffSave:
                    onFinish(eff.availability)
                    
                default: break
                }
            }
        ).foregroundColorKMM(.companion.DarkGrey)
    }
}

private struct Content: View {
    let state: Feature.State
    let listener: (Feature.Msg) -> Void
    let onCancel: () -> Void
    
    @State
    private var editingRepeat = false
    
    var body: some View {
        VStack(spacing: 0) {
            NavigationBar(
                title: .init(view: Text(state.title).opacity(editingRepeat ? 0 : 1)),
                leftItem: .init(
                    viewBuilder: {
                        if !editingRepeat {
                            Text(Feature.Strings.shared.cancel)
                        } else {
                            HStack {
                                Image(systemName: "chevron.left")
                                Text(state.title)
                            }
                        }
                    },
                    handler: {
                        if editingRepeat {
                            editingRepeat = false
                        } else {
                            onCancel()
                        }
                    }
                ),
                rightItem: .init(
                    view: Text(state.finishButtonTitle).opacity(editingRepeat ? 0 : 1),
                    enabled: state.valid,
                    handler: editingRepeat ? nil : {
                        listener(Feature.MsgSave())
                    }
                )
            )
            if !editingRepeat {
                content
                    .transition(.asymmetric(insertion: .move(edge: .leading), removal: .move(edge: .trailing)))
            } else {
                editRepeatView
                    .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .move(edge: .leading)))
            }
        }.animation(.default, value: editingRepeat)
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
                Button(
                    action: {
                        editingRepeat = true
                    }
                ) {
                    HStack {
                        Text(Feature.Strings().repeat)
                        Spacer().fillMaxWidth()
                        Text(state.availability.repeat.title)
                        Image(systemName: "chevron.right")
                    }.foregroundColorKMM(.companion.DarkGrey)
                }
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
    
    var editRepeatView: some View {
        List {
            ForEach(Repeat.companion.allCases, id: \.self) { value in
                Button(
                    action: {
                        editingRepeat = false
                        listener(Feature.MsgSetRepeat(repeat: value))
                    }
                ) {
                    HStack {
                        Text(value.title.lowercased())
                        Spacer()
                        if value == state.availability.repeat {
                            Image(systemName: "checkmark")
                                .foregroundColorKMM(.companion.Green)
                        }
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
