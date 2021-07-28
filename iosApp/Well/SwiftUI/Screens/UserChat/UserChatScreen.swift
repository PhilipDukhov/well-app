//
// Created by Phil on 16.06.2021.
//

import SwiftUI
import SharedMobile

struct UserChatScreen: View {
    let state: UserChatFeature.State
    let listener: (UserChatFeature.Msg) -> Void

    @State private var text = ""
    @State private var lastId: Int?

    var body: some View {
        NavigationBar(
            leftItem: NavigationBarItem(
                view: leftItem(),
                handler: {
                    listener(UserChatFeature.MsgBack())
                }
            ),
            rightItem: NavigationBarItem(
                view: Image(systemName: "phone.fill")
                    .font(.system(size: 20))
                    .foregroundColorKMM(ColorConstants.White)
                    .padding(),
                enabled: state.user != nil,
                handler: {
                    listener(UserChatFeature.MsgCall())
                }
            )
        )
        ChatsList(messages: state.messages) { message in
            print("send MsgMarkMessageRead \(message)")
            listener(UserChatFeature.MsgMarkMessageRead(message: message))
        }
        Spacer(minLength: 0)
        userInput()
    }

    @ViewBuilder
    func userInput() -> some View {
        HStack {
            Button {
                listener(UserChatFeature.MsgChooseImage())
            } label: {
                Image(systemName: "photo")
            }
            TextField("Message", text: $text, onCommit: send)
                .style(.body2Light)
                .foregroundColorKMM(ColorConstants.Black)
                .padding(.vertical, 3)
                .padding(.horizontal, 8)
                .background(
                    Capsule()
                )
                .fillMaxWidth()
            Button(action: send) {
                Image(systemName: "arrow.up.circle.fill")
            }
                .disabled(text.isEmpty)
                .opacity(text.isEmpty ? 0.4 : 1)
        }
            .foregroundColorKMM(ColorConstants.White)
            .font(.system(size: 25))
            .padding(.horizontal)
            .padding(.vertical, 3)
            .background(GradientView(gradient: .main))
    }

    @ViewBuilder
    func leftItem() -> some View {
        HStack {
            Image(systemName: "chevron.left")
                .font(.system(size: 20))
                .foregroundColorKMM(ColorConstants.White)
                .padding()
            if let user = state.user {
                ProfileImage(user)
                    .frame(size: 40)
                Text(user.fullName)
                    .style(.subtitle2)
            }
        }
    }

    private func send() {
        listener(UserChatFeature.MsgSendMessage(string: text))
        text = ""
    }
}

private struct ChatsList: View {
    let messages: [ChatMessageWithStatus]
    let markRead: (ChatMessage) -> Void

    @State
    private var visibleMessages = Set<Int32>()
    @State
    private var scrollToFirstNeeded = false
    @State
    private var scrollToBottomButtonVisible = false

    var body: some View {
        let firstId = messages.first?.message.id
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                ScrollViewReader { scrollView in
                    LazyVStack(spacing: 8) {
                        ForEach(messages, id: \.message.id) { message in
                            let id = message.message.id
                            ChatMessageCell(message: message)
                                .id(id)
                                .onAppear {
                                    visibleMessages.insert(id)
                                    if message.status == ChatMessageWithStatus.Status.incomingunread {
                                        markRead(message.message)
                                    }
                                }
                                .onDisappear {
                                    visibleMessages.remove(id)
                                }
                                .rotation3DEffect(.radians(.pi), axis: (x: 1, y: 0, z: 0))
                        }
                    }
                        .onChange(of: scrollToFirstNeeded) { scrollToFirstNeeded in
                            if let id = firstId,
                               scrollToFirstNeeded {
                                print(".onChange(of: scrollToFirstNeeded)")
                                withAnimation {
                                    scrollView.scrollTo(id)
                                }
                                self.scrollToFirstNeeded = false
                            }
                        }
                        .onChange(of: visibleMessages) { visibleMessages in
                            let scrollToBottomButtonVisible = firstId != nil && !visibleMessages.contains(firstId!)
                            if self.scrollToBottomButtonVisible != scrollToBottomButtonVisible {
                                withAnimation {
                                    print("self.scrollToBottomButtonVisible = scrollToBottomButtonVisible \(self.scrollToBottomButtonVisible) \(scrollToBottomButtonVisible)")
                                    self.scrollToBottomButtonVisible = scrollToBottomButtonVisible
                                }
                            }
                        }
                        .padding()
                }
            }.rotation3DEffect(.radians(.pi), axis: (x: 1, y: 0, z: 0))
            if scrollToBottomButtonVisible {
                Button {
                    scrollToFirstNeeded = true
                } label: {
                    Image(systemName: "chevron.down.circle.fill")
                        .foregroundColorKMM(ColorConstants.Green)
                        .font(.system(size: 25))
                        .padding()
                }
            }
        }
    }
}
