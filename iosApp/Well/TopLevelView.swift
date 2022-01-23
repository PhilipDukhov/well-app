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
        case let screen as TopLevelFeatureStateScreenSingle:
            screenView(screen: screen.screen)
            
        case let tabsScreen as TopLevelFeatureStateScreenTabs:
            TabBarController(
                items: tabsScreen.tabs,
                tabKeyPath: \.tab,
                selectedTab: state.selectedTab,
                setSelectedTab: { tabScreen in
                    listener(TopLevelFeature.MsgSelectTab(tab: tabScreen))
                },
                tabBarItem: { tab in
                    UITabBarItem(
                        title: tab.spacedName(),
                        image: tab.icon(),
                        tag: 0
                    )
                }, badgeValue: { tabScreen in
                    let unreadCount = (tabScreen.screen as? ScreenState.ChatList)?.state.unreadCount.toInt()
                    ?? (tabScreen.screen as? ScreenState.Calendar)?.state.unreadCount.toInt() ?? 0
                    return unreadCount > 0 ? "\(unreadCount)" : nil
                }, contentView: { tabScreen in
                    VStack(spacing: 0) {
                        screenView(screen: tabScreen.screen)
                    }
                }
            ).ignoresSafeArea()

                .accentColor(SwiftUI.Color(hex: 0x1B3D6D))

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
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.Login:
            LoginScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.MyProfile:
            MyProfileScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.Experts:
            ExpertsScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.Call:
            CallScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.More:
            MoreScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.About:
            AboutScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.Support:
            SupportScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.WellAcademy:
            WellAcademyScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.ChatList:
            ChatListScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.UserChat:
            UserChatScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        case let state as ScreenState.Calendar:
            CalendarScreen(state: state.state) {
                listener(state.mapMsgToTopLevel(msg: $0))
            }

        default:
            EmptyView()
        }
    }
}

private extension TopLevelFeature.StateTab {
    func icon() -> UIImage? {
        switch self {
        case .myprofile:
            return UIImage(systemName: "person.fill")
        case .experts:
            return UIImage(named: "expertTab")
        case .chatlist:
            return UIImage(systemName: "message.fill")
        case .calendar:
            return UIImage(systemName: "calendar")
        case .more:
            return UIImage(named: "moreTab")

        default:
            fatalError("need image for tab \(self)")
        }
    }
}
