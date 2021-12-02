//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct TextStyle: Equatable {
    let fontSize: CGFloat
    let fontWeight: Font.Weight

    static let h3 = Self(fontSize: 37, fontWeight: .regular)
    static let h4 = Self(fontSize: 30, fontWeight: .bold)
    static let h5 = Self(fontSize: 25, fontWeight: .regular)
    static let subtitle1 = Self(fontSize: 21, fontWeight: .regular)
    static let subtitle2 = Self(fontSize: 17, fontWeight: .bold)
    static let body1 = Self(fontSize: 18, fontWeight: .regular)
    static let body1Light = Self(fontSize: 18, fontWeight: .regular)
    static let body2 = Self(fontSize: 16, fontWeight: .regular)
    static let body2Light = Self(fontSize: 16, fontWeight: .light)
    static let caption = Self(fontSize: 13, fontWeight: .regular)
    static let captionLight = Self(fontSize: 13, fontWeight: .light)

    var uiFont: UIFont {
        .systemFont(ofSize: fontSize, weight: fontWeight.toUIFontWeight())
    }

    var font: Font {
        .system(size: fontSize, weight: fontWeight)
    }

    func copy(fontSize: CGFloat? = nil, fontWeight: Font.Weight? = nil) -> Self {
        .init(fontSize: fontSize ?? self.fontSize, fontWeight: fontWeight ?? self.fontWeight)
    }
}

extension Text {
    @inline(__always) func textStyle(
        _ style: TextStyle
    ) -> Text {
        font(style.font)
    }
}

extension View {
    @inline(__always) func textStyle(
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
