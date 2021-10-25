//
// Created by Philip Dukhov on 1/14/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct DrawShape: Shape {
    let points: [CGPoint]
    let touchTolerance: CGFloat

    func path(
        in rect: CGRect
    ) -> Path {
        var path = Path()
        guard points.count > 1 else {
            return path
        }
        path.move(to: points[0])
        var lastPoint = points[0]
        points.enumerated().dropFirst().forEach { i, point in
            if i < points.endIndex,
               hypot(lastPoint.x - point.x, lastPoint.y - point.y) >= touchTolerance {
                path.addQuadCurve(
                    to: .init(
                        x: (point.x + lastPoint.x) / 2,
                        y: (point.y + lastPoint.y) / 2
                    ),
                    control: lastPoint
                )
            } else {
                path.addLine(to: point)
            }
            lastPoint = point
        }
        return path
    }
}
