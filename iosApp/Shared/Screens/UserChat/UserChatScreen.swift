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
                view: leftItem()
            ),
            rightItem: NavigationBarItem(
                view: Image(systemName: "phone.fill")
                    .font(.system(size: 20))
                    .foregroundColorKMM(.companion.White)
                    .padding(),
                enabled: state.user != nil,
                handler: {
                    listener(UserChatFeature.MsgCall())
                }
            )
        )
        ChatsList(messages: state.messages) { message in
            listener(UserChatFeature.MsgMarkMessageRead(messageId: message.id))
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
                .textStyle(.body2Light)
                .foregroundColorKMM(.companion.Black)
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
            .foregroundColorKMM(.companion.White)
            .font(.system(size: 25))
            .padding(.horizontal)
            .padding(.vertical, 3)
            .background(GradientView(gradient: .main))
    }

    @ViewBuilder
    func leftItem() -> some View {
        HStack {
            Control(action: {
                listener(UserChatFeature.MsgBack())
            }) {
                Image.systemChevronLeft
                    .foregroundColorKMM(.companion.White)
                    .padding()
            }
            if let user = state.user {
                Control(action: {
                    listener(UserChatFeature.MsgOpenUserProfile())
                }) {
                    HStack {
                        ProfileImage(user)
                            .frame(size: 40)
                        Text(user.fullName)
                            .textStyle(.subtitle2)
                    }
                }
            }
        }
    }

    private func send() {
        listener(UserChatFeature.MsgSendMessage(string: text))
        text = ""
    }
}

private struct ChatsList: View {
    let messages: [ChatMessageViewModel]
    let markRead: (ChatMessageViewModel) -> Void

    @State
    private var visibleMessages = Set<Int64>()
    @State
    private var scrollToFirstNeeded = false
    @State
    private var scrollToBottomButtonVisible = false

    var body: some View {
        let firstId = messages.first?.id
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                ScrollViewReader { scrollView in
                    LazyVStack(spacing: 8) {
                        ForEach(messages, id: \.id) { message in
                            let id = message.id
                            ChatMessageCell(message: message)
                                .id(id)
                                .onAppear {
                                    visibleMessages.insert(id)
                                    if message.status == .incomingunread {
                                        markRead(message)
                                    }
                                }
                                .onDisappear {
                                    visibleMessages.remove(id)
                                }
                                .rotation3DEffect(.radians(.pi), axis: (x: 1, y: 0, z: 0))
                        }
                    }
                        .onChange(of: scrollToFirstNeeded) { scrollToFirstNeeded in
                            if let id = firstId, scrollToFirstNeeded {
                                Napier.i(".onChange(of: scrollToFirstNeeded)")
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
                                    Napier.i("self.scrollToBottomButtonVisible = scrollToBottomButtonVisible \(self.scrollToBottomButtonVisible) \(scrollToBottomButtonVisible)")
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
                        .foregroundColorKMM(.companion.Green)
                        .font(.system(size: 25))
                        .padding()
                }
            }
        }
    }
}
