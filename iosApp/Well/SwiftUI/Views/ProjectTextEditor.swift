//
// Created by Phil on 29.05.2021.
//

import SwiftUI

struct ProjectTextEditor: View {
    let placeholder: String?
    @Binding
    var text: String

    var body: some View {
        ZStack(alignment: .topLeading) {
            TextEditor(text: $text)
                .fillMaxWidth()
                .padding(.vertical, 10)
                .padding(.horizontal, 15)
            if text.isEmpty, let placeholder = placeholder {
                HStack {
                    Text(placeholder)
                        .allowsHitTesting(false)
                        .foregroundColorKMM(ColorConstants.LightGray)
                        .padding(.vertical, 18)
                        .padding(.horizontal, 15 + 4)
                    Spacer()
                }.allowsHitTesting(false)
            }
        }.fillMaxWidth()
            .frame(minHeight: 150)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(ColorConstants.LightGray.toColor(), lineWidth: 2)
                    .padding(1)
            )
    }
}
