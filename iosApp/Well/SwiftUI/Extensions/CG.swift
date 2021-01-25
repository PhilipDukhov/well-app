//
// Created by Philip Dukhov on 1/15/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import CoreGraphics

extension CGRect {
    var mid: CGPoint {
        CGPoint(x: midX, y: midY)
    }
}

extension Int32 {
    func toCGFloat() -> CGFloat {
        CGFloat(self)
    }
}

extension CGSize {
    init(size: CGFloat) {
        self.init(width: size, height: size)
    }
}

extension CGFloat {
    func toFloat() -> Float {
        Float(self)
    }
}

extension Float {
    func toCGFloat() -> CGFloat {
        CGFloat(self)
    }
}