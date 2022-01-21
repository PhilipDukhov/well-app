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
    let message: ChatMessageViewModel

    @State
    private var fullscreenImage: ChatMessage.ContentImage?

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
        }.sheet(item: $fullscreenImage) {
            FullscreenImage(image: $0) {
                fullscreenImage = nil
            }
        }
    }

    @ViewBuilder
    private func contentView(content: ChatMessage.Content) -> some View {
        switch message.content {
        case let content as ChatMessage.ContentImage:
            let width = UIScreen.main.bounds.width / 2.5
            VStack(alignment: .trailing) {
                SharedImage(
                    url: URL(string: content.url)!,
                    placeholder: ProgressView()
                )
                    .frame(
                        width: width,
                        height: width / content.aspectRatio.toCGFloat()
                    )
                    .onTapGesture {
                        fullscreenImage = content
                    }.clipped()
                dateAndStatus()
            }

        case let content as ChatMessage.ContentText:
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

private struct FullscreenImage: View {
    let image: ChatMessage.ContentImage
    let dismiss: () -> Void
    @State
    private var isSharePresented = false

    var body: some View {
        VStack {
            NavigationBar(
                leftItem: .cancel(handler: dismiss),
                rightItem: .init(
                    viewBuilder: {
                        Image(systemName: "square.and.arrow.up")
                    }, handler: {
                        isSharePresented = true
                    }
                )
            )
            Spacer().fillMaxHeight()
            let remoteUrl = URL(string: image.url)!
            SharedImage(
                url: remoteUrl,
                placeholder: ProgressView(),
                processImage: { view, uiImage in
                    view.background{
                        HalfScreenActivityView(activityItems: [uiImage], isPresented: $isSharePresented)
                    }
                },
                aspectRatio: image.aspectRatio.toCGFloat(),
                contentMode: .fit
            ).layoutPriority(1)
            Spacer().fillMaxHeight()
        }
    }
}

private extension CGFloat {
    static let minSpacerWidth: CGFloat = 30
    static let horizontalPadding: CGFloat = 10
}
