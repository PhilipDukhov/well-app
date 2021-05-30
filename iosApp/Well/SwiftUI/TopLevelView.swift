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
            case let screen as TopLevelFeature.StateScreenSingle:
                screenView(screen: screen.screen)

            case let tabsScreen as TopLevelFeature.StateScreenTabs:
                TabView(selection: Binding<TopLevelFeature.StateTab>(get: { state.selectedTab }, set: { tab in
                    listener(TopLevelFeature.MsgSelectTab(tab: tab))
                })) {
                    ForEachEnumerated(tabsScreen.tabs) { i, tabScreen in
                        VStack(spacing: 0) {
                            screenView(screen: tabScreen.screen)
                        }
                            .tabItem {
                                tabScreen.tab.icon()
                                Text("\(tabScreen.tab)")
                            }
                            .tag(tabScreen.tab)
                    }
                }.accentColor(SwiftUI.Color(hex: 0x1B3D6D))

            default:
                fatalError("state.currentScreen unexpected \(state.currentScreen)")
            }
        }
    }

    @ViewBuilder
    func screenView(screen: ScreenState) -> some View {
        switch screen {
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

        case let state as ScreenState.More:
            MoreScreen(state: state.state) {
                listener(TopLevelFeature.MsgMoreMsg(msg: $0))
            }

        case let state as ScreenState.About:
            AboutScreen(state: state.state) {
                listener(TopLevelFeature.MsgAboutMsg(msg: $0))
            }

        case let state as ScreenState.Support:
            SupportScreen(state: state.state) {
                listener(TopLevelFeature.MsgSupportMsg(msg: $0))
            }

        default:
            EmptyView()
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

private extension TopLevelFeature.StateTab {
    func icon() -> Image {
        switch self {
        case .myprofile:
            return Image(systemName: "person.fill")
        case .experts:
            return Image(uiImage: R.image.profile.expert()!)
        case .more:
            return Image(systemName: "ellipsis")

//            calendar "My calendar"
//            person.badge.plus "Invite a colleague"
//            building.columns "WELL Academy"
//            clock.arrow.circlepath "Activity history"
//            face.smiling "Sponsor & donate"
//            wrench "Technical support"
//            info.circle "About"

        default:
            fatalError("need image for tab \(self)")
        }
    }
}
