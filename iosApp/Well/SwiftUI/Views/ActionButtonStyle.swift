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

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .style(.title1)
            .foregroundColorKMM(foreground)
            .frame(height: 57)
            .fillMaxWidth()
            .background(background)
            .clipShape(Capsule())
    }

    private var foreground: SharedMobile.Color {
        switch style {
        case .onWhite:
            return ColorConstants.White

        case .white:
            return ColorConstants.MediumBlue
        }
    }

    private var background: some View {
        switch style {
        case .onWhite:
            return ColorConstants.Green.toColor()

        case .white:
            return ColorConstants.White.toColor()
        }
    }
}
