//
//  TopLevelView.swift
//  Well
//
//  Created by Philip Dukhov on 12/27/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct TopLevelView: View {
    let state: TopLevelFeature.State
    let listener: (TopLevelFeature.Msg) -> Void
    
    var body: some View {
        switch state.currentScreen {
        case let state as TopLevelFeature.StateScreenStateOnlineUsers:
            OnlineUsersScreen(state: state.state) {
                listener(TopLevelFeature.MsgOnlineUsersMsg(msg: $0))
            }

        case let state as TopLevelFeature.StateScreenStateCall:
            CallScreen(state: state.state) {
                listener(TopLevelFeature.MsgCallMsg(msg: $0))
            }

        default:
            Text("not handler state: \(state.currentScreen)")
        }
    }
}
