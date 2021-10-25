//
//  Bundle+displayName.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

extension Bundle {
    var displayName: String {
        // swiftlint:disable:next force_cast
        object(forInfoDictionaryKey: kCFBundleNameKey as String) as! String
    }
}
