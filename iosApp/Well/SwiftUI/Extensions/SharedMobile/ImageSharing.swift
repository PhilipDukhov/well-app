//
// Created by Philip Dukhov on 1/19/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import CoreGraphics
import SwiftUI
import SharedMobile

extension DrawingFeature.State {
    func strokeStyle(lineWidth: CGFloat) -> StrokeStyle {
        StrokeStyle(
            lineWidth: lineWidth,
            lineCap: nativeStrokeStyle.lineCap.toCGLineCap(),
            lineJoin: nativeStrokeStyle.lineJoin.toCGLineJoin(),
            miterLimit: 10
        )
    }
}

extension DrawingFeature.StateStrokeStyleLineCap {
    func toCGLineCap() -> CGLineCap {
        switch self {
        case .round: return .round
        case .butt: return .butt
        case .square: return .square
        default: fatalError()
        }
    }
}

extension DrawingFeature.StateStrokeStyleLineJoin {
    func toCGLineJoin() -> CGLineJoin {
        switch self {
        case .round: return .round
        case .bevel: return .bevel
        case .miter: return .miter
        default: fatalError()
        }
    }
}

extension CallFeature.StateVideoViewPosition {
    func sizeIn(geometry: GeometryProxy) -> CGSize {
        switch self {
        case .fullscreen:
            return CGSize(size: .infinity)
            
        case .minimized:
            let width = geometry.size.width / 3
            let height = width * 1920 / 1080
            return CGSize(width: width, height: height)
            
        default: fatalError()
        }
    }
}
