//
// Created by Phil on 29.05.2021.
//

import SwiftUI
import SharedMobile

struct SupportScreen: View {
    let state: SupportFeature.State
    let listener: (SupportFeature.Msg) -> Void

    @State
    private var text = ""

    var body: some View {
        NavigationBar(
            title: "Support",
            leftItem: NavigationBarItem(view: Image(systemName: "chevron.left")) {
                listener(SupportFeature.MsgBack())
            }
        )

        VStack {
            Text(state.text)
                .textStyle(.body1)
                .layoutPriority(3)
            ProjectTextEditor(placeholder: "Please write your message", text: $text)
            Spacer()
                .layoutPriority(2)
            Button(action: {
                listener(SupportFeature.MsgSend(text: text))
            }) {
                Text("Send")
            }.buttonStyle(ActionButtonStyle(style: .onWhite))
                .disabled(!(1...state.maxCharacters.toInt()).contains(text.count))
        }.padding()
    }
}
