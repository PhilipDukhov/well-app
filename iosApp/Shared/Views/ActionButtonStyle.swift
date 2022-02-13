//
// Created by Phil on 06.05.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ActionButtonStyle: ButtonStyle {
    enum Style {
        case white
        case onWhite
    }

    let style: Style
    @Environment(\.isEnabled)
    private var isEnabled: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .textStyle(.subtitle1)
            .foregroundColorKMM(foreground)
            .frame(height: 57)
            .fillMaxWidth()
            .background(background.opacity(isEnabled ? 1 : 0.4))
            .opacity(configuration.isPressed ? 0.8 : 1)
            .clipShape(Capsule())
            .animation(.easeOut(duration: 0.2), value: configuration.isPressed)
    }

    private var foreground: SharedMobile.Color {
        switch style {
        case .onWhite:
            return .companion.White

        case .white:
            return .companion.MediumBlue
        }
    }

    @ViewBuilder
    private var background: some View {
        switch style {
        case .onWhite:
            GradientView(gradient: .actionButton)

        case .white:
            ColorConstants.White.toColor()
        }
    }
}
