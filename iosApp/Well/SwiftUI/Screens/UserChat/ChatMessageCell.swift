//
// Created by Phil on 20.06.2021.
//

import SwiftUI
import SharedMobile

struct ChatMessageCell: View {
    let message: ChatMessageWithStatus

    var body: some View {
        let incoming = message.status.isIncoming
        HStack(spacing: 0) {
            if !incoming {
                Spacer(minLength: .minSpacerWidth)
            }
            contentView(content: message.message.content)
                .foregroundColor(.white)
                .padding(EdgeInsets(top: 5, leading: incoming ? 22 : 17, bottom: 5, trailing: incoming ? 17 : 22))
                .background(
                    Image("bubble")
                        .foregroundColorKMM(ColorConstants.LightBlue)
                        .rotation3DEffect(.degrees(incoming ? 180 : 0), axis: (x: 0, y: 1, z: 0))
                )
            if incoming {
                Spacer(minLength: .minSpacerWidth)
            }
        }
    }

    private func dateAndStatus() -> some View {
        HStack(spacing: 0) {
            Text(message.date + " ")
            message.status.icon()
        }.font(.system(size: 7))
    }

    @ViewBuilder
    private func contentView(content: ChatMessage.Content) -> some View {
        switch message.message.content {
        case let content as ChatMessage.ContentImage:
            SharedImage(url: NSURL(string: content.url)!, placeholder: ActivityIndicator(), aspectRatio: content.aspectRatio?.toCGFloat())

        case let content as ChatMessage.ContentText:
            ZStack(alignment: .bottomTrailing) {
                VStack(alignment: .leading) {
                    Text("\(message.message.id); \(message.message.creation); " + content.text)
                        .style(.body4)
                    dateAndStatus()
                        .font(.system(size: 7))
                        .foregroundColor(.clear)
                }
                dateAndStatus()
                    .foregroundColor(.white)
            }

        default: fatalError("ChatMessage.Content unexpected: \(content)")
        }
    }
}

private extension ChatMessageWithStatus.Status {
    @ViewBuilder
    func icon() -> some View {
        switch self {
        case .incomingread, .incomingunread: EmptyView()
        case .outgoingsending:
            Image(systemName: "timer")
        case .outgoingsent:
            Image(systemName: "checkmark")
        case .outgoingread:
            Image("double-checkmark")
        default: fatalError("unexpected ChatMessageWithStatus.Status: \(self.name)")
        }
    }
}

private extension CGFloat {
    static let minSpacerWidth: CGFloat = 30
    static let horizontalPadding: CGFloat = 10
}
