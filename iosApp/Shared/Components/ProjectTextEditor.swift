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
            let showingPlaceholder = text.isEmpty && placeholder != nil
            if showingPlaceholder {
                TextEditor(text: .constant(placeholder!))
                    .font(.body)
                    .foregroundColorKMM(.companion.LightGray)
                    .disabled(true)
            }
            TextEditor(text: $text)
                .fillMaxWidth()
                .opacity(showingPlaceholder ? 0.25 : 1)
        }
        .padding(.vertical, 10)
        .padding(.horizontal, 15)
        .fillMaxWidth()
            .frame(minHeight: 150)
            .overlay(
                Shapes.medium
                    .strokeColorKMM(.companion.LightGray, lineWidth: 2)
                    .padding(1)
            )
    }
}
