//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct TextStyle: Equatable {
    let fontWeight: Font.Weight
    let fontSize: CGFloat

    static let h4 = Self(fontWeight: .bold, fontSize: 30)
    static let title1 = Self(fontWeight: .regular, fontSize: 21)
    static let title2 = Self(fontWeight: .bold, fontSize: 17)
    static let body1 = Self(fontWeight: .regular, fontSize: 18)
    static let body2 = Self(fontWeight: .regular, fontSize: 17)
    static let body3 = Self(fontWeight: .regular, fontSize: 16)
    static let body4 = Self(fontWeight: .light, fontSize: 16)
    static let caption1 = Self(fontWeight: .regular, fontSize: 14)
    static let caption2 = Self(fontWeight: .light, fontSize: 12)

    var uiFont: UIFont {
        .systemFont(ofSize: fontSize, weight: fontWeight.toUIFontWeight())
    }

    var font: Font {
        .system(size: fontSize, weight: fontWeight)
    }
}

extension Text {
    @inline(__always) func style(
        _ style: TextStyle
    ) -> Text {
        font(style.font)
    }
}

extension View {
    @inline(__always) func style(
        _ style: TextStyle
    ) -> some View {
        font(style.font)
    }
}

private extension Font.Weight {
    func toUIFontWeight() -> UIFont.Weight {
        switch self {
        case .ultraLight: return .ultraLight
        case .thin: return .thin
        case .light: return .light
        case .regular: return .regular
        case .medium: return .medium
        case .semibold: return .semibold
        case .bold: return .bold
        case .heavy: return .heavy
        case .black: return .black
        default:
            fatalError("\(self) weight not provided")
        }
    }
}
