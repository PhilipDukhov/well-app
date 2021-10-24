//
//  TestingScreen.swift
//  Well
//
//  Created by Phil on 07.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

#if DEBUG
import SwiftUI
import SharedMobile

struct TestingScreen: View {
    private enum Screen {
        case profile
        case availabilityCalendar
        case call
        case filter
        
        case other
    }
    
    private static let screen: Screen? = .availabilityCalendar
    static let testing = screen != nil
    
//    let messages = ChatMessageWithStatus.companion.getTestMessagesWithStatus(count: 100)
    
    var body: some View {
//        switch Self.screen {
//        case .none:
            EmptyView()
//
//        case .availabilityCalendar:
//            ReducerView(
//                CurrentUserAvailabilitiesListFeature().testState(),
//                reducer: CurrentUserAvailabilitiesListFeature().reducer,
//                view: CurrentUserAvailabilityView.init
//            )
//        case .profile:
//            ProfileTestView()
//
//        case .call:
//            ReducerView(
//                CallFeature().testState(status: .ongoing),
//                reducer: CallFeature().reducer,
//                view: CallScreen.init
//            )
//        case .filter:
//            ReducerView(
//                FilterFeature.State(filter: UsersFilter.Companion().default(searchString: "")),
//                reducer: FilterFeature().reducer
//            ) {
//                FilterScreen(state: $0, listener: $1) {
//                }
//            }
//
//        case .other:
//            ScrollView {
//                LazyVStack {
//                    ForEachIndexed(messages) { i, message in
//                        ChatListCell(
//                            item: .init(
//                                user: User.companion.testUser,
//                                lastMessage: message,
//                                unreadCount: Int32(i - 1) * Int32(i - 1)
//                            )
//                        )
//                    }
//                }
//            }
//        }
    }
}

//struct ProfileTestView: View {
//    @AppStorage("ProfileTestView_isCurrent") var isCurrent = false
//
//    var body: some View {
//        ZStack(alignment: .bottomTrailing) {
//            VStack {
//                ViewModelView(
//                    MyProfileTestModel(isCurrent: isCurrent),
//                    view: MyProfileScreen.init
//                )
//                    .id(isCurrent)
//            }
//            Toggle(isOn: $isCurrent, label: {})
//        }
//    }
//}
#endif
