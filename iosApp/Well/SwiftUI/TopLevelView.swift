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
        content
            .environment(\.defaultMinListRowHeight, 0)
    }
    static let testing = false

    @ViewBuilder
    var content: some View {
        if Self.testing {
            callScreen()
        } else {
            switch state.currentScreen {
            case let state as TopLevelFeature.StateScreenStateOnlineUsers:
                OnlineUsersScreen(state: state.state) {
                    listener(TopLevelFeature.MsgOnlineUsersMsg(msg: $0))
                }.onTapGesture {
                    print("OnlineUsersScreen tap")
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

    @State var callState = CallFeature().testState(status: .ongoing)

    @ViewBuilder
    func callScreen() -> some View {
        CallScreen(state: callState) {
            callState = CallFeature().reducer(msg: $0, state: callState).first!
        }
    }

//    @State var sharingState = ImageSharingFeature().testState(
//        imageContainer: ImageContainer(uiImage: UIImage(named: "testImage")!)
//    )
//
//    @ViewBuilder
//    func sharingScreen() -> some View {
//        ImageSharingScreen(state: sharingState) { msg in
//            let timeInterval: TimeInterval
//            (sharingState, timeInterval) = timeCounter.count {
//                ImageSharingFeature().reducerMeasuring(msg: msg, state: sharingState).first!
//            }
//            NSLog("\(msg) \(timeInterval) \(sharingState.lastReduceDurations) \(sharingState.canvasPaths.map { $0.points.count ?? 0 }.reduce(0, +))")
//            let lastSecondCounted = timeCounter.lastSecondCounted
//            if lastSecondCounted * 60 > 1 {
//                NSLog("too long", lastSecondCounted)
//            }
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
        return times.map{$0.1}.reduce(0, +) / (times[0].0.timeIntervalSince(times.last!.0))
    }
}
