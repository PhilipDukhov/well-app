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

    func toHexEncodedString() -> String {
        map { String(format: "%02hhx", $0) }.joined()
    }
}

extension Collection {
    var isNotEmpty: Bool {
        !isEmpty
    }

    @inlinable public func filterNot(_ isNotIncluded: (Element) throws -> Bool) rethrows -> [Element] {
        try filter { try !isNotIncluded($0) }
    }
}

extension Sequence {
    @inlinable public func firstNotNullOfOrNull<R>(of transform: (Self.Element) throws -> R?) rethrows -> R? {
        for element in self {
            let result = try transform(element)
            if let result = result {
                return result
            }
        }
        return nil
    }

    func anySatisfy(_ predicate: (Element) throws -> Bool) rethrows -> Bool {
        for char in self {
            if try predicate(char) {
                return true
            }
        }
        return false
    }
}

extension CustomStringConvertible {
    func toString() -> String {
        String(describing: self)
    }
}

extension String {
    var isNotBlank: Bool {
        !isEmpty && anySatisfy { !$0.isWhitespace }
    }
}
