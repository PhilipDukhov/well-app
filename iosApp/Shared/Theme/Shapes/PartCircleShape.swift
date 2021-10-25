//
// Created by Philip Dukhov on 1/18/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct PartCircleShape: Shape {
    var part: CGFloat

    var animatableData: CGFloat {
        get { part }
        set { part = newValue }
    }

    func path(in rect: CGRect) -> Path {
        Path { path in
            let side = min(rect.width, rect.height)
            let size = CGSize(
                width: (rect.width - side) * (1 - part) + side,
                height: (rect.height - side) * (1 - part) + side
            )
            path.addRoundedRect(
                in: .init(
                    origin: .init(
                        x: rect.midX - size.width / 2,
                        y: rect.midY - size.height / 2
                    ),
                    size: size
                ),
                cornerSize: CGSize(
                    width: size.width / 2 * part,
                    height: size.height / 2 * part
                )
            )
        }
    }
}
