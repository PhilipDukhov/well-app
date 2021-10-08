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
    static let testing = true
    let messages = ChatMessageWithStatus.companion.getTestMessagesWithStatus(count: 100)
    
    var body: some View {
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

struct FilterTestView: View {
    @State var state = FilterFeature.State(filter: UsersFilter.Companion().default(searchString: ""))
    
    var body: some View {
        FilterScreen(state: state) { msg in
            state = FilterFeature().reducer(msg: msg, state: state).first!
        } hide: {}
    }
}

struct CallTestView: View {
    @State var state = CallFeature().testState(status: .ongoing)
    
    var body: some View {
        CallScreen(state: state) {
            state = CallFeature().reducer(msg: $0, state: state).first!
        }
    }
}

struct ProfileTestView: View {
    @State var state = MyProfileFeature().testState()
    
    var body: some View {
        MyProfileScreen(state: state) {
            state = MyProfileFeature().reducer(msg: $0, state: state).first!
        }
    }
}

#endif
