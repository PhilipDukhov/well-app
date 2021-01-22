//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SharedMobile
import SwiftUI

extension ServerModelsUser {
    var profileImageURL: URL? {
        profileImageUrl.flatMap { URL(string: $0) }
    }
}

extension ServerModelsColor {
    func toColor() -> Color {
        Color(hex: UInt(argb), alpha: Double(alpha))
    }
}

extension CGPoint {
    func toServerModelsPoint() -> ServerModelsPoint {
        ServerModelsPoint(x: Float(x), y: Float(y))
    }
}

extension ServerModelsPoint {
    func toCGPoint() -> CGPoint {
        CGPoint(x: CGFloat(x), y: CGFloat(y))
    }
}

extension ServerModelsSize {
    func toCGSize() -> CGSize {
        CGSize(width: CGFloat(width), height: CGFloat(height))
    }
}

extension CGSize {
    func toSize() -> ServerModelsSize {
        .init(width: Float(width), height_: Float(height))
    }
}

extension CGSize {
    func toNativeScaleSize() -> ServerModelsSize {
        .init(
            width: Float(width * UIScreen.main.nativeScale),
            height_: Float(height * UIScreen.main.nativeScale)
        )
    }
}
