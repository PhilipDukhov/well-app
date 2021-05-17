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
        VStack(spacing: 0) {
            content
                .environment(\.defaultMinListRowHeight, 0)
                .statusBar(style: .lightContent)
        }
    }
    #if DEBUG
        static let testing = false
    #else
        static let testing = false
    #endif

    @ViewBuilder
    var content: some View {
        if Self.testing {
            filterScreen()
        } else {
            switch state.currentScreen {
            case let state as ScreenState.Welcome:
                WelcomeScreen(state: state.state) {
                    listener(TopLevelFeature.MsgWelcomeMsg(msg: $0))
                }

            case is ScreenState.Launch:
                EmptyView()

            case let state as ScreenState.Login:
                LoginScreen(state: state.state) {
                    listener(TopLevelFeature.MsgLoginMsg(msg: $0))
                }

            case let state as ScreenState.MyProfile:
                MyProfileScreen(state: state.state) {
                    listener(TopLevelFeature.MsgMyProfileMsg(msg: $0))
                }

            case let state as ScreenState.Experts:
                ExpertsScreen(state: state.state) {
                    listener(TopLevelFeature.MsgExpertsMsg(msg: $0))
                }

            case let state as ScreenState.Call:
                CallScreen(state: state.state) {
                    listener(TopLevelFeature.MsgCallMsg(msg: $0))
                }

            default:
                EmptyView()
            }
        }
    }

    @State var filterState = FilterFeature.State(filter: UsersFilter.Companion().default(searchString: ""))
    @ViewBuilder
    func filterScreen() -> some View {
//        FilterScreen(state: filterState) { msg in
//            filterState = FilterFeature().reducer(msg: msg, state: filterState).first!
//        }
    }

//    @State var callState = CallFeature().testState(status: .ongoing)
//    @State var profileState = MyProfileFeature().testState()

//    @ViewBuilder
//    func callScreen() -> some View {
//        CallScreen(state: callState) {
//            callState = CallFeature().reducer(msg: $0, state: callState).first!
//        }
//    }
//    @ViewBuilder
//    func profileScreen() -> some View {
//        MyProfileScreen(state: profileState) {
//            profileState = MyProfileFeature().reducer(msg: $0, state: profileState).first!
//        }
//    }
}

let timeCounter = TimeCounter()

final class TimeCounter {
    var times = [(Foundation.Date, TimeInterval)]()

    func count<R>(block: () -> R) -> (R, TimeInterval) {
        let date = Foundation.Date()
        let result = block()
        let timeInterval = -date.timeIntervalSinceNow
        times.append((date, timeInterval))
        return (result, timeInterval)
    }

    var lastSecondCounted: TimeInterval {
        times = times.filter {
            $0.0.timeIntervalSinceNow < 1
        }
        return times.map {
            $0.1
        }.reduce(0, +) / (times[0].0.timeIntervalSince(times.last!.0))
    }
}
