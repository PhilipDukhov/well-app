//
//  EnumDictionary.swift
//  Well
//
//  Created by Philip Dukhov on 10/1/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

struct EnumDictionary<Key, Value> where Key: Hashable & CaseIterable {
    private var dictionary: [Key: Value]
    
    init(block: (Key) -> Value) {
        dictionary = Key.allCases.reduce(into: [:]) { $0[$1] = block($1) }
    }
    
    subscript(key: Key) -> Value {
        get { dictionary[key]! }
        set { dictionary[key] = newValue }
    }
    
    var values: [Value] { Array(dictionary.values) }
    
    @inlinable func forEach(_ body: ((key: Key, value: Value)) throws -> Void) rethrows {
        try dictionary.forEach(body)
    }
}
