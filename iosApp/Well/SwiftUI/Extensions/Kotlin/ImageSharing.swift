//
// Created by Philip Dukhov on 1/19/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import CoreGraphics
import SwiftUI
import SharedMobile

extension ImageSharingFeature.State {
    func strokeStyle(lineWidth: CGFloat) -> StrokeStyle {
        StrokeStyle(
            lineWidth: lineWidth,
            lineCap: nativeStrokeStyle.lineCap.toCGLineCap(),
            lineJoin: nativeStrokeStyle.lineJoin.toCGLineJoin(),
            miterLimit: 10
        )
    }
}

extension ImageSharingFeature.StateStrokeStyleLineCap {
    func toCGLineCap() -> CGLineCap {
        switch self {
        case .round: return .round
        case .butt: return .butt
        case .square: return .square
        default: fatalError()
        }
    }
}

extension ImageSharingFeature.StateStrokeStyleLineJoin {
    func toCGLineJoin() -> CGLineJoin {
        switch self {
        case .round: return .round
        case .bevel: return .bevel
        case .miter: return .miter
        default: fatalError()
        }
    }
}
