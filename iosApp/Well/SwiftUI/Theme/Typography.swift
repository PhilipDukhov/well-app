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
}

extension Text {
    @inline(__always) func style(
        _ style: TextStyle
    ) -> Text {
        font(.system(size: style.fontSize, weight: style.fontWeight))
    }
}

extension View {
    @inline(__always) func style(
        _ style: TextStyle
    ) -> some View {
        font(.system(size: style.fontSize, weight: style.fontWeight))
    }
}

extension Font.Weight {
    func toUIWeight() -> UIFont.Weight {
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
