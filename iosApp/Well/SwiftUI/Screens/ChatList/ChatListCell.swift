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
                        .style(.caption)
                    Spacer()
                    Text(item.lastMessage.date)
                        .style(.captionLight)
                        .foregroundColorKMM(ColorConstants.DarkGrey)
                }
                HStack(spacing: 7) {
                    Text(item.lastMessage.message.contentDescription())
                        .style(.captionLight)
                        .foregroundColorKMM(ColorConstants.DarkGrey)
                    Spacer()
                    if item.unreadCount > 0 {
                        Text("\(item.unreadCount)")
                            .style(.body2)
                            .foregroundColorKMM(ColorConstants.White)
                            .padding(.horizontal, 5)
                            .background(
                                HorizontalCapsule().foregroundColorKMM(ColorConstants.Green)
                            )
                    }
                }
                Spacer()
                Divider()
            }.padding(.trailing)
        }.backgroundColorKMM(item.unreadCount > 0 ? ColorConstants.Green10 : ColorConstants.White)
    }
}
