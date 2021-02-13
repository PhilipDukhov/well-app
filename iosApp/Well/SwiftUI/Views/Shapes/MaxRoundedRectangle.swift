//
//  MaxRoundedRectangle.swift
//  Well
//
//  Created by Philip Dukhov on 2/11/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct MaxRoundedRectangle: Shape {
    func path(in rect: CGRect) -> Path {
        Path { path in
            path.addRoundedRect(
                in: rect,
                cornerSize: CGSize(size: min(rect.height, rect.width) / 2)
            )
        }
    }
}
