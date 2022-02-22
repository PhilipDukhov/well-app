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
    @State
    private var includeLogs = true

    var body: some View {
        VStack(spacing: 0) {
            NavigationBar(
                title: state.title,
                leftItem: .back {
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
                Toggle(state.includeLogs, isOn: $includeLogs)
                Button(action: {
                    listener(SupportFeature.MsgSend(text: text, includeLogs: includeLogs))
                }) {
                    Text("Send")
                }.buttonStyle(ActionButtonStyle(style: .onWhite))
                    .disabled(!(1...state.maxCharacters.toInt()).contains(text.count))
            }.padding()
        }.overlay {
            if state.processing {
                SharedMobile.Color.companion.InactiveOverlay.toColor()
                    .ignoresSafeArea()
                ProgressView()
            }
        }
    }
}
