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
        AppContainer(content: VStack(spacing: 0, content: content))
    }

    @ViewBuilder
    func content() -> some View {
        switch state.currentScreen {
        case let screen as TopLevelFeature.StateScreenSingle:
            screenView(screen: screen.screen)
            
        case let tabsScreen as TopLevelFeature.StateScreenTabs:
            TabView(selection: Binding<TopLevelFeature.StateTab>(get: {
                state.selectedTab
            }, set: { tab in
                listener(TopLevelFeature.MsgSelectTab(tab: tab))
            })) {
                ForEach(tabsScreen.tabs, id: \.tab) { tabScreen in
                    VStack(spacing: 0) {
                        screenView(screen: tabScreen.screen)
                    }
                    .tabItem {
                        tabScreen.tab.icon()
                        Text(tabScreen.tab.spacedName())
                    }
                    .tag(tabScreen.tab)
                }
            }.accentColor(SwiftUI.Color(hex: 0x1B3D6D))
            
        default:
            fatalError("state.currentScreen unexpected \(state.currentScreen)")
        }
    }

    @ViewBuilder
    func screenView(screen: ScreenState) -> some View {
        switch screen {
        case is ScreenState.Launch:
            EmptyView()
            
        case let state as ScreenState.Welcome:
            WelcomeScreen(state: state.state) {
                listener(TopLevelFeature.MsgWelcomeMsg(msg: $0))
            }

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

        case let state as ScreenState.ChatList:
            ChatListScreen(state: state.state) {
                listener(TopLevelFeature.MsgChatListMsg(msg: $0))
            }

        case let state as ScreenState.UserChat:
            UserChatScreen(state: state.state) {
                listener(TopLevelFeature.MsgUserChatMsg(msg: $0))
            }

        default:
            EmptyView()
        }
    }
}

private extension TopLevelFeature.StateTab {
    @ViewBuilder
    func icon() -> some View {
        switch self {
        case .myprofile:
            Image(systemName: "person.fill")
        case .experts:
            Image("expertTab")
        case .chatlist:
            Image(systemName: "message.fill")
        case .more:
            Image("moreTab")

        default:
            fatalError("need image for tab \(self)")
        }
    }
}
