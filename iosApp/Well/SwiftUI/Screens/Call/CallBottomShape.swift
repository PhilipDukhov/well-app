//
// Created by Philip Dukhov on 1/15/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct CallBottomShape: Shape {
    let radius: CGFloat
    let offset: CGFloat

    func path(in rect: CGRect) -> Path {
        Path { path in
            path.addRect(rect)
            path.addPath(.init(UIBezierPath(
                arcCenter: .init(x: rect.midX, y: rect.minY - offset),
                radius: radius,
                startAngle: 0,
                endAngle: .pi,
                clockwise: true
            ).reversing().cgPath))
        }
    }
}
