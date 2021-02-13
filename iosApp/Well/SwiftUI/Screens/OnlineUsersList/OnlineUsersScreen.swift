//
//  OnlineUsersScreen.swift
//  Well
//
//  Created by Philip Dukhov on 12/3/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Combine

struct OnlineUsersScreen: View {
    let state: OnlineUsersFeature.State
    let listener: (OnlineUsersFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: state.connectionStatus.stringRepresentation,
            rightItem: state.currentUser.map {
                NavigationBarItem(
                    view: ProfileImage($0).frame(size: 45),
                    handler: listener(OnlineUsersFeature.MsgOnCurrentUserSelected())
                )
            }
        )
        List {
            ForEach(state.users, id: \.id) { user in
                UserCell(viewModel: user) {
                    listener(OnlineUsersFeature.MsgOnUserSelected(user: user))
                } onCallButtonTap: {
                    listener(OnlineUsersFeature.MsgOnCallUser(user: user))
                }
            }
        }.listSeparatorStyle(.none)
    }
}
