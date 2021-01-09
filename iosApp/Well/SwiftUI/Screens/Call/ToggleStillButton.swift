//
// Created by Philip Dukhov on 1/3/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct ToggleStillButton: View {
    enum Size {
        case standard
        case small

        var size: CGFloat {
            switch self {
            case .standard:
                return 50
            case .small:
                return 42
            }
        }
    }

    let systemImage: String
    let size: Size
    let handler: () -> Void

    var selected = true

    private let secondaryColor = Color.white.opacity(0.3)

    var body: some View {
        if selected {
            Circle()
                .fill(secondaryColor)
                .frame(size: size.size)
                .overlay(image)
                .foregroundColor(.white)
        } else {
            Circle()
                .strokeBorder(lineWidth: 2)
                .overlay(image)
                .frame(size: size.size)
                .foregroundColor(secondaryColor)
                .onTapGesture(perform: handler)
        }
    }

    @ViewBuilder
    var image: some View {
        Image(systemName: systemImage)
            .font(.system(size: size.size / 2))
    }
}
