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
            .clipShape(Capsule())
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
            GradientView(gradient: .main)

        case .white:
            ColorConstants.White.toColor()
        }
    }
}
