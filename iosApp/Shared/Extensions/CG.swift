//
// Created by Philip Dukhov on 1/15/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import CoreGraphics
import SharedMobile

extension CGRect {
    var mid: CGPoint {
        CGPoint(x: midX, y: midY)
    }
}

extension CGSize {
    init(size: CGFloat) {
        self.init(width: size, height: size)
    }

    var maxDimension: CGFloat {
        max(width, height)
    }
    
    var aspectRatio: CGFloat {
        width / height
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

    func toDouble() -> Double {
        Double(self)
    }
}

extension Int32 {
    func toCGFloat() -> CGFloat {
        CGFloat(self)
    }
}

extension Int32 {
    func toDouble() -> Double {
        Double(self)
    }

    func toInt() -> Int {
        Int(self)
    }
}

extension NSNumber {
    func toCGFloat() -> CGFloat {
        CGFloat(truncating: self)
    }
}
