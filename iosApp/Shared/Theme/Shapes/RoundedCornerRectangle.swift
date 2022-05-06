//
//  RoundedCornerRectangle.swift
//  Well
//
//  Created by Phil on 15.11.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCornerRectangle(radius: radius, corners: corners))
    }
}

struct RoundedCornerRectangle: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> SwiftUI.Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

extension UIRectCorner {
    static let top: UIRectCorner = [.topLeft, .topRight]
}
