//
//  TestScreens.swift
//  LocalTest
//
//  Created by Phil on 25.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct TestingScreens: View {
    let selectedScreen: TestScreen
    
    let messages = ChatMessageViewModel.companion.getTestMessagesWithStatus(count: 100)

    @State
    var padding: CGFloat = 0

    var body: some View {
        switch selectedScreen {

        case .welcome:
            ReducerView(
                initial: WelcomeFeature.State(),
                reducer: WelcomeFeature().reducer,
                view: WelcomeScreen.init
            )

        case .availabilitycalendar:
            ReducerView(
                initial: AvailabilitiesCalendarFeature().testState(count: 0),
                reducer: AvailabilitiesCalendarFeature().reducer,
                view: AvailabilitiesCalendarView.init
            ).padding(.bottom, padding)
                .id(padding)
            Slider(value: $padding, in: 0...200)
        case .myprofile:
            ProfileTestView()

        case .call:
            ReducerView(
                initial: CallFeature().testState(status: .ongoing),
                reducer: CallFeature().reducer,
                view: CallScreen.init
            )
        case .expertsfilter:
            ReducerView(
                initial: FilterFeature.State(filter: UsersFilter.companion.default(searchString: "")),
                reducer: FilterFeature().reducer
            ) {
                FilterScreen(state: $0, listener: $1) {
                }
            }
        case .calendar:
            ReducerView(
                initial: CalendarFeature.State.companion.testState,
                reducer: CalendarFeature().reducer
            ) {
                CalendarScreen(state: $0, listener: $1)
            }
        case .favorites:
            ReducerView(
                initial: FavoritesFeature().testState(),
                reducer: FavoritesFeature().reducer
            ) {
                FavoritesScreen(state: $0, listener: $1)
            }
        case .donate:
            ReducerView(
                initial: DonateFeature.State(),
                reducer: DonateFeature().reducer
            ) {
                DonateScreen(state: $0, listener: $1)
            }
            
        case .local:
            Image("bubble")
                .frame(width: 200, height: 100)

        default:
            fatalError("not implemented \(selectedScreen)")
        }
    }
}

struct ProfileTestView: View {
    @EnvironmentObject
    var systemHelper: SystemHelper
    @AppStorage("ProfileTestView_isCurrent") var isCurrent = false

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack {
                ViewModelView(
                    MyProfileTestModel(isCurrent: isCurrent, systemHelper: systemHelper),
                    view: MyProfileScreen.init
                )
                    .id(isCurrent)
            }
            Toggle(isOn: $isCurrent, label: {})
        }
    }
}
