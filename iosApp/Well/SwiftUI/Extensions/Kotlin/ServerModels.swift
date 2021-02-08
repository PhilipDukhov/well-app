//
// Created by Philip Dukhov on 12/29/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SharedMobile
import SwiftUI

extension User {
    var profileImageURL: URL? {
        profileImageUrl.flatMap { URL(string: $0) }
    }
}

extension SharedMobile.Color {
    func toColor() -> SwiftUI.Color {
        Color(hex: UInt(argb), alpha: Double(alpha))
    }
}

extension CGPoint {
    func toPoint() -> Point {
        Point(x: Float(x), y: Float(y))
    }
}

extension Point {
    func toCGPoint() -> CGPoint {
        CGPoint(x: CGFloat(x), y: CGFloat(y))
    }
}

extension Size {
    func toCGSize() -> CGSize {
        CGSize(width: CGFloat(width), height: CGFloat(height))
    }
}

extension CGSize {
    func toSize() -> Size {
        .init(width: Float(width), height_: Float(height))
    }
}

extension CGSize {
    func toNativeScaleSize() -> Size {
        .init(
            width: Float(width * UIScreen.main.nativeScale),
            height_: Float(height * UIScreen.main.nativeScale)
        )
    }
}

// swiftlint:disable:next identifier_name
let ColorConstants = SharedMobile.Color.Companion()
