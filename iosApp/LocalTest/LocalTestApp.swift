//
//  LocalTestApp.swift
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
    
    case other
}

@main
struct LocalTestApp: App {
    var body: some Scene {
        WindowGroup {
            TestingScreen()
        }
    }
}

struct TestingScreen: View {
    @State
    var selectedScreen: Screen = .availabilityCalendar
    
    @State
    var opened = true
    
    let messages = ChatMessageWithStatus.companion.getTestMessagesWithStatus(count: 100)
    
    var body: some View {
        NavigationView {
            VStack {
                ScrollViewReader { scrollViewReader in
                    ScrollView(.horizontal) {
                        LazyHStack {
                            ForEach(Screen.allCases, id: \.self) { screen in
                                Button(screen.rawValue.capitalized) {
                                    selectedScreen = screen
                                    opened = true
                                }.buttonStyle(.bordered)
                            }
                        }.onAppear {
                        }.onChange(of: selectedScreen) { screen in
                            scrollViewReader.scrollTo(screen, anchor: .center)
                        }.onVolumeChange {
                            opened = false
                        }
                    }
                }
                Spacer().fillMaxHeight()
                NavigationLink(isActive: $opened) {
                    screen.navigationBarHidden(true)
                } label: {
                    Text("Open")
                }
                Spacer().fillMaxHeight()
            }.navigationBarHidden(true)
        }
    }
    
    @ViewBuilder
    var screen: some View {
        switch selectedScreen {
        case .availabilityCalendar:
            ReducerView(
                CurrentUserAvailabilitiesListFeature().testState(),
                reducer: CurrentUserAvailabilitiesListFeature().reducer,
                view: CurrentUserAvailabilityView.init
            )
        case .profile:
            ProfileTestView()

        case .call:
            ReducerView(
                CallFeature().testState(status: .ongoing),
                reducer: CallFeature().reducer,
                view: CallScreen.init
            )
        case .filter:
            ReducerView(
                FilterFeature.State(filter: UsersFilter.Companion().default(searchString: "")),
                reducer: FilterFeature().reducer
            ) {
                FilterScreen(state: $0, listener: $1) {
                }
            }

        case .other:
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
