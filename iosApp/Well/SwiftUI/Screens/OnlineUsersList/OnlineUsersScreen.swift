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
        ZStack(alignment: .trailing) {
            Text(state.connectionStatus.stringRepresentation)
                .frame(maxWidth: .infinity, alignment: .center)
            state.currentUser.map {
                ProfileImage($0)
                    .frame(size: 45)
            }
        }.frame(maxWidth: .infinity, alignment: .trailing)
            .frame(height: 45)
            .padding()
        Divider()
        List {
            ForEach(state.users, id: \.id) { user in
                UserCell(viewModel: user)
                    .onTapGesture {
                        print("tap ok \(Date().timeIntervalSince1970)")
                        listener(OnlineUsersFeature.MsgOnUserSelected(user: user))
                    }
            }
        }
    }
}
