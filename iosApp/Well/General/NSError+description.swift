//
//  NSError+description.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

extension NSError {
    convenience init(description: String, code: Int = 0) {
        self.init(domain: Bundle.main.displayName, code: code, userInfo: [NSLocalizedDescriptionKey: description])
    }
}
