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
    
    static let initial: Screen = .profile
}

struct TestingScreens: View {
    let selectedScreen: Screen
    
    let messages = ChatMessageWithStatus.companion.getTestMessagesWithStatus(count: 100)
    
    var body: some View {
        switch selectedScreen {
        case .availabilityCalendar:
            ReducerView(
                initial: CurrentUserAvailabilitiesListFeature().testState(),
                reducer: CurrentUserAvailabilitiesListFeature().reducer,
                view: CurrentUserAvailabilityView.init
            )
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
            EmptyView()
        }
    }
}

struct ProfileTestView: View {
    @AppStorage("ProfileTestView_isCurrent") var isCurrent = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack {
                ViewModelView(
                    MyProfileTestModel(isCurrent: isCurrent),
                    view: MyProfileScreen.init
                )
                    .id(isCurrent)
            }
            Toggle(isOn: $isCurrent, label: {})
        }
    }
}
