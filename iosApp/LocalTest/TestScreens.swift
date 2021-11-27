//
//  TestScreens.swift
//  LocalTest
//
//  Created by Phil on 25.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

enum Screen: String, CaseIterable {
    case profile
    case availabilityCalendar = "Availability Calendar"
    case call
    case filter
    case chatMessagesList
    
    case local
    
    static let initial: Screen = .call
}

struct TestingScreens: View {
    let selectedScreen: Screen
    
    let messages = ChatMessageViewModel.companion.getTestMessagesWithStatus(count: 100)

    @State
    var padding: CGFloat = 0

    var body: some View {
        switch selectedScreen {
        case .availabilityCalendar:
            ReducerView(
                initial: AvailabilitiesCalendarFeature().testState(count: 100),
                reducer: AvailabilitiesCalendarFeature().reducer,
                view: AvailabilitiesCalendarView.init
            ).padding(.bottom, padding)
                .id(padding)
            Slider(value: $padding, in: 0...200)
        case .profile:
            ProfileTestView()

        case .call:
            ReducerView(
                initial: CallFeature().testState(status: .ongoing),
                reducer: CallFeature().reducer,
                view: CallScreen.init
            )
        case .filter:
            ReducerView(
                initial: FilterFeature.State(filter: UsersFilter.Companion().default(searchString: "")),
                reducer: FilterFeature().reducer
            ) {
                FilterScreen(state: $0, listener: $1) {
                }
            }

        case .chatMessagesList:
            ScrollView {
                LazyVStack {
                    ForEachIndexed(messages) { i, message in
                        ChatListCell(
                            item: .init(
                                user: User.companion.testUser,
                                lastMessage: message,
                                unreadCount: Int32(i - 1) * Int32(i - 1)
                            )
                        )
                    }
                }
            }
            
        case .local:
            Image("bubble")
                .frame(width: 200, height: 100)
        }
    }
}

struct ProfileTestView: View {
    @EnvironmentObject
    var contextHelper: ContextHelper
    @AppStorage("ProfileTestView_isCurrent") var isCurrent = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack {
                ViewModelView(
                    MyProfileTestModel(isCurrent: isCurrent, contextHelper: contextHelper),
                    view: MyProfileScreen.init
                )
                    .id(isCurrent)
            }
            Toggle(isOn: $isCurrent, label: {})
        }
    }
}
