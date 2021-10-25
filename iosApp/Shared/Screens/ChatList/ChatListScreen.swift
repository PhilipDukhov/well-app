//
// Created by Phil on 16.06.2021.
//

import SwiftUI
import SharedMobile

struct ChatListScreen: View {
    let state: ChatListFeature.State
    let listener: (ChatListFeature.Msg) -> Void

    var body: some View {
        NavigationBar(
            title: "Chat list"
        )
        ScrollView {
            LazyVStack {
                ForEachIndexed(state.listItems) { _, listItem in
                    Button {
                        listener(ChatListFeature.MsgSelectChat(userId: listItem.user.id))
                    } label: {
                        ChatListCell(item: listItem)
                    }
                }
            }
        }
        Spacer()
    }
}
