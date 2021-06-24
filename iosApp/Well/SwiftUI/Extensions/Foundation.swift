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
}

extension Data {
    func toString(encoding: String.Encoding) -> String? {
        String(data: self, encoding: encoding)
    }
}

extension Collection {
    var isNotEmpty: Bool {
        !isEmpty
    }
}
