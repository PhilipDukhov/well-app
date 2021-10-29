//
//  Napier.swift
//  Well
//
//  Created by Phil on 07.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile
import SwiftUI

extension Napier {
    #if DEBUG
    @inline(__always)
    static func printUI(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) -> some View {
        log(logLevel: .debug, tag: tag, items, separator: separator, file: file, function: function)
        return EmptyView()
    }
    #endif
    
    static func v(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) {
        log(logLevel: .verbose, tag: tag, items, separator: separator, file: file, function: function)
    }
    
    static func d(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) {
        log(logLevel: .debug, tag: tag, items, separator: separator, file: file, function: function)
    }
    
    static func i(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) {
        log(logLevel: .info, tag: tag, items, separator: separator, file: file, function: function)
    }
    
    static func w(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) {
        log(logLevel: .warning, tag: tag, items, separator: separator, file: file, function: function)
    }
    
    static func e(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) {
        log(logLevel: .error, tag: tag, items, separator: separator, file: file, function: function)
    }
    
    static func a(tag: String? = nil, _ items: Any..., separator: String = " ", file: String = #file, function: String = #function) {
        log(logLevel: .assert, tag: tag, items, separator: separator, file: file, function: function)
    }
    
    // swiftlint:disable:next function_parameter_count
    static fileprivate func log(logLevel: LogLevel, tag: String?, _ items: [Any], separator: String, file: String, function: String) {
        let message = items.map { "\($0)" }.joined(separator: separator)
        shared.log(
            priority: logLevel,
            tag: tag ?? {
                let fileName = URL(fileURLWithPath: file).lastPathComponent
                let functionName: String
                if let firstBraceIndex = function.firstIndex(of: "(") {
                    functionName = String(function[..<firstBraceIndex])
                } else {
                    functionName = function
                }
                return "\(fileName):\(functionName)"
            }(),
            throwable: nil,
            message_: message
        )
    }
}
