//
//  ForceNavControllerBarHidden.swift
//  LocalTest
//
//  Created by Phil on 16.11.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import UIKit

extension UINavigationController {

    static let swizzleIsNavigationBarHiddenImplementation: Void = {
        // Accessor
        let c = UINavigationController.self
        method_exchangeImplementations(
            class_getInstanceMethod(c, #selector(getter: UINavigationController.isNavigationBarHidden))!,
            class_getInstanceMethod(c, #selector(getter: swizzledIsNavigationBarHidden))!
        )
        method_exchangeImplementations(
            class_getInstanceMethod(c, #selector(setter: UINavigationController.isNavigationBarHidden))!,
            class_getInstanceMethod(c, #selector(setter: swizzledIsNavigationBarHidden))!
        )
        method_exchangeImplementations(
            class_getInstanceMethod(c, #selector(UINavigationController.setNavigationBarHidden))!,
            class_getInstanceMethod(c, #selector(swizzledSetNavigationBarHidden))!
        )
    }()

    @objc var swizzledIsNavigationBarHidden: Bool {
        get {
            self.swizzledIsNavigationBarHidden
        }
        // swiftlint:disable:next unused_setter_value
        set {
            self.swizzledIsNavigationBarHidden = true
        }
    }

    @objc func swizzledSetNavigationBarHidden(_ hidden: Bool, animated: Bool) {
        swizzledSetNavigationBarHidden(true, animated: animated)
    }
}
