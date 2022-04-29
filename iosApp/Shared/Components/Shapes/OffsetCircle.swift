//
// Created by Philip Dukhov on 1/15/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct OffsetCircle: Shape {
    let offset: CGFloat

    func path(
        in rect: CGRect
    ) -> Path {
        var path = Path()
        path.addArc(
            center: rect.mid,
            radius: rect.width / 2 - offset,
            startAngle: .radians(.zero),
            endAngle: .radians(.pi * 2),
            clockwise: true
        )
        return path
    }
}
