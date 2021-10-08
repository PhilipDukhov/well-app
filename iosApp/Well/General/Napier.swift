//
//  Napier.swift
//  Well
//
//  Created by Phil on 07.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile

extension Napier {
    static func d(_ items: Any..., separator: String = " ") {
        shared.d(message: items.joined(separator: separator), throwable: nil, tag_: nil)
    }
    
    static func w(_ items: Any..., separator: String = " ") {
        shared.w(message: items.joined(separator: separator), throwable: nil, tag_: nil)
    }
    
    static func e(_ items: Any..., separator: String = " ") {
        shared.e(message: items.joined(separator: separator), throwable: nil, tag_: nil)
    }
    
    static func i(_ items: Any..., separator: String = " ") {
        shared.i(message: items.joined(separator: separator), throwable: nil, tag_: nil)
    }
}

private extension Array where Element == Any {
    func joined(separator: String = "") -> String {
        map { "\($0)" }.joined(separator: separator)
    }
}
