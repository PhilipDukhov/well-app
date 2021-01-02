//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct TextStyle {
    let fontWeight: Font.Weight
    let fontSize: CGFloat

    static let h4 = Self(fontWeight: .bold, fontSize: 30)
    static let body1 = Self(fontWeight: .regular, fontSize: 18)
}

extension Text {
    @inline(__always) func style(
            _ style: TextStyle
    ) -> some View {
        font(.system(size: style.fontSize))
                .fontWeight(style.fontWeight)
    }
}
