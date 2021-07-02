//
// Created by Phil on 20.06.2021.
//

import SwiftUI
import SharedMobile

extension ChatMessage.ContentImage: Identifiable {
    public var id: String {
        url
    }
}

struct ChatMessageCell: View {
    let message: ChatMessageWithStatus

    @State
    private var fullscreenImage: ChatMessage.ContentImage?

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
                        .foregroundColorKMM(incoming ? ColorConstants.LightBlue : ColorConstants.LightGray)
                        .rotation3DEffect(.degrees(incoming ? 180 : 0), axis: (x: 0, y: 1, z: 0))
                )
            if incoming {
                Spacer(minLength: .minSpacerWidth)
            }
        }
            .sheet(item: $fullscreenImage) { fullscreenImage in
                SharedImage(url: NSURL(string: fullscreenImage.url)!,
                    placeholder: ActivityIndicator(),
                    aspectRatio: fullscreenImage.aspectRatio?.toCGFloat(),
                    contentMode: .fit
                )
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
            SharedImage(
                url: NSURL(string: content.url)!,
                placeholder: ActivityIndicator(),
                aspectRatio: content.aspectRatio?.toCGFloat()
            )
                .frame(width: UIScreen.main.bounds.width / 2.5)
                .onTapGesture {
                    fullscreenImage = content
                }

        case let content as ChatMessage.ContentText:
            ZStack(alignment: .bottomTrailing) {
                // two dateAndStatus hack to align text left and date right
                VStack(alignment: .leading, spacing: 0) {
                    Text(content.text)
                        .style(.body4)
                    dateAndStatus()
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
