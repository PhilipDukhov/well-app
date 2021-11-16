//
// Created by Phil on 19.05.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import UIKit

extension UIApplication {
    func endEditing() {
        sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
    }
}

extension CGFloat {
    func roundedScreenScaled(_ rule: FloatingPointRoundingRule = .toNearestOrAwayFromZero) -> Self {
        (self * UIScreen.main.nativeScale).rounded(rule) / UIScreen.main.nativeScale
    }
}
