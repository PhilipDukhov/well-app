//
//  UsersList.swift
//  Well
//
//  Created by Phil on 13.02.2022.
//  Copyright © 2022 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct UsersList: View {
    let users: [User]
    let onSelect: (User) -> Void
    let onToggleFavorite: (User) -> Void

    var body: some View {
        ScrollView {
            if !users.isEmpty {
                Divider()
            }
            Rectangle()
                .foregroundColor(.white)
                .frame(height: 1)
            LazyVStack {
                ForEach(users, id: \.id) { user in
                    UserCell(user: user) {
                        onToggleFavorite(user)
                    }.onTapGesture {
                        onSelect(user)
                    }
                    Divider()
                }
            }
        }
    }
}
