//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct TextStyle: Equatable {
    let fontWeight: Font.Weight
    let fontSize: CGFloat

    static let h4 = Self(fontWeight: .bold, fontSize: 30)
    static let subtitle1 = Self(fontWeight: .regular, fontSize: 21)
    static let subtitle2 = Self(fontWeight: .bold, fontSize: 17)
    static let body1 = Self(fontWeight: .regular, fontSize: 18)
    static let body1Light = Self(fontWeight: .regular, fontSize: 18)
    static let body2 = Self(fontWeight: .regular, fontSize: 16)
    static let body2Light = Self(fontWeight: .light, fontSize: 16)
    static let caption = Self(fontWeight: .regular, fontSize: 13)
    static let captionLight = Self(fontWeight: .light, fontSize: 13)

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
