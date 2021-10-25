//
//  HorizontalCapsule.swift
//  Well
//
//  Created by Phil on 07.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct HorizontalCapsule: Shape {
    func path(in rect: CGRect) -> SwiftUI.Path {
        let finalRect = rect.insetBy(
            dx: min(rect.width - rect.height, 0) / 2,
            dy: 0
        )
        return SwiftUI.Path(
            roundedRect: finalRect,
            cornerSize: CGSize(size: min(finalRect.width, finalRect.height))
        )
    }
}
