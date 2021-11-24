//
// Created by Phil on 20.06.2021.
//

import SwiftUI
import SharedMobile

extension ChatMessageViewModel.ContentImage: Identifiable {
    public var id: String {
        url
    }
}

struct ChatMessageCell: View {
    let message: ChatMessageViewModel

    @State
    private var fullscreenImage: ChatMessageViewModel.ContentImage?

    var body: some View {
        let incoming = message.status.isIncoming
        HStack(spacing: 0) {
            if !incoming {
                Spacer(minLength: .minSpacerWidth)
            }
            contentView(content: message.content)
                .foregroundColor(.white)
                .padding(EdgeInsets(top: 5, leading: incoming ? 22 : 17, bottom: 5, trailing: incoming ? 17 : 22))
                .background(
                    Image("bubble")
                        .foregroundColorKMM(incoming ? .companion.LightBlue : .companion.LightGray)
                        .rotation3DEffect(.degrees(incoming ? 180 : 0), axis: (x: 0, y: 1, z: 0))
                )
            if incoming {
                Spacer(minLength: .minSpacerWidth)
            }
        }.sheet(item: $fullscreenImage) { fullscreenImage in
            SharedImage(
                url: NSURL(string: fullscreenImage.url)!,
                placeholder: ProgressView(),
                aspectRatio: fullscreenImage.aspectRatio.toCGFloat(),
                contentMode: .fit
            )
        }
    }

    @ViewBuilder
    private func contentView(content: ChatMessageViewModel.Content) -> some View {
        switch message.content {
        case let content as ChatMessageViewModel.ContentImage:
            let width = UIScreen.main.bounds.width / 2.5
            VStack(alignment: .trailing) {
                SharedImage(
                    url: NSURL(string: content.url)!,
                    placeholder: ProgressView()
                )
                    .frame(
                        width: width,
                        height: width / content.aspectRatio.toCGFloat()
                    )
                    .onTapGesture {
                        fullscreenImage = content
                    }
                dateAndStatus()
            }

        case let content as ChatMessageViewModel.ContentText:
            ZStack(alignment: .bottomTrailing) {
                // two dateAndStatus hack to align text left and date right
                VStack(alignment: .leading, spacing: 0) {
                    Text(content.string)
                        .textStyle(.body2Light)
                    dateAndStatus()
                        .foregroundColor(.clear)
                }
                dateAndStatus()
            }

        case let content as ChatMessageViewModel.ContentMeeting:
            VStack(alignment: .trailing) {
                VStack(alignment: .leading) {
                    Text(UserChatFeature.Strings.shared.meetingScheduled)
                        .textStyle(.body1Light)
                    content.meeting.map { meeting in
                        Text(UserChatFeature.Strings.shared.meetingStars(meeting: meeting))
                            .textStyle(.body2Light)
                    }
                }
                dateAndStatus()
            }

        default: fatalError("ChatMessage.Content unexpected: \(content)")
        }
    }

    private func dateAndStatus() -> some View {
        HStack(spacing: 0) {
            Text(message.date + " ")
            message.status.icon()
        }.font(.system(size: 7))
    }
}

private extension ChatMessageViewModel.Status {
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
