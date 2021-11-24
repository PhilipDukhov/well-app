//
// Created by Phil on 21.06.2021.
//

import SwiftUI
import SharedMobile

struct ChatListCell: View {
    let item: ChatListFeature.StateListItem

    var body: some View {
        HStack {
            ProfileImage(item.user).frame(size: 50).padding()
            VStack {
                Spacer()
                HStack {
                    Text(item.user.fullName)
                        .textStyle(.caption)
                    Spacer()
                    Text(item.lastMessage.date)
                        .textStyle(.captionLight)
                        .foregroundColorKMM(.companion.DarkGrey)
                }
                HStack(spacing: 7) {
                    Text(
                        ChatListFeature
                            .Strings.shared
                            .messageContentDescription(
                                content: item.lastMessage.content
                            )
                    )
                        .textStyle(.captionLight)
                        .foregroundColorKMM(.companion.DarkGrey)
                    Spacer()
                    if item.unreadCount > 0 {
                        Text("\(item.unreadCount)")
                            .textStyle(.body2)
                            .foregroundColorKMM(.companion.White)
                            .padding(.horizontal, 5)
                            .background(
                                HorizontalCapsule().foregroundColorKMM(.companion.Green)
                            )
                    }
                }
                Spacer()
                Divider()
            }.padding(.trailing)
        }.backgroundColorKMM(item.unreadCount > 0 ? .companion.Green10 : .companion.White)
    }
}
