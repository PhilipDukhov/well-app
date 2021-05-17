//
//  Foundation.swift
//  Well
//
//  Created by Philip Dukhov on 2/13/21.
//  Copyright © 2021 Well. All rights reserved.
//

import Foundation

extension String {
    func toURL() -> URL? {
        URL(string: self)
    }

    var isNotEmpty: Bool {
        !isEmpty
    }
}
