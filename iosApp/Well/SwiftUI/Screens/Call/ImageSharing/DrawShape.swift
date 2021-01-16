//
// Created by Philip Dukhov on 1/14/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct DrawShape: Shape {
    var points: [CGPoint]

    func path(
        in rect: CGRect
    ) -> Path {
        var path = Path()
        if points.count > 1 {
            path.move(to: points[0])
            path.addLines(Array(points.dropFirst()))
        }
        return path
    }
}
