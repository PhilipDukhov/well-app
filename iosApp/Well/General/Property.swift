//
//  Property.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

final class Property<T> {
    
    var value: T {
        didSet {
            subscribers.forEach{ $0(value) }
        }
    }
    var subscribers: [(T) -> Void] = []
    
    init(_ value: T) {
        self.value = value
    }
    
    func subscribe(skipFirst: Bool = false, callBack: @escaping (T) -> Void) {
        subscribers.append(callBack)
        guard !skipFirst else { return }
        callBack(value)
    }
}

extension Property where T: Equatable {
    func subscribeNoRepeat(callBack: @escaping (T) -> Void) {
        var previous = value
        let checkDuplicate: (T) -> Void = { newValue in
            guard previous != newValue else { return }
            
            previous = newValue
            callBack(newValue)
        }
        
        subscribers.append(checkDuplicate)
        callBack(value)
    }
    
    var noRepeat: Property<T> {
        var previousValue = value
        let noRepeatProperty = Property<T>(previousValue)
        subscribe { newValue in
            guard previousValue != newValue else { return }
            
            previousValue = newValue
            noRepeatProperty.value = newValue
        }
        
        return noRepeatProperty
    }
}

extension Property {
    func queue(_ queue: DispatchQueue) -> Property<T> {
        let property = Property<T>(value)
        
        subscribe { newValue in
            queue.async {
                property.value = newValue
            }
        }
        
        return property
    }
}

extension Property {
    /// Subscribe to property with debounce
    ///
    /// - Parameters:
    ///     - interval: interval for debounce in milliseconds
    ///     - queue: result queue
    ///     - callBack: callBack
    func subscribeWithdebounce(interval milliseconds: Int, queue: DispatchQueue, callBack: @escaping (T) -> Void) {
        let debounced = Function.debounce(interval: milliseconds, queue: queue, function: callBack)
        subscribe(callBack: debounced)
    }
}

enum Function {
    static func debounce<T>(interval milliseconds: Int, queue: DispatchQueue, function: @escaping (T) -> Void) -> ((T) -> Void) {
        let limitDelay = DispatchTimeInterval.milliseconds(milliseconds)
        var workItem: DispatchWorkItem?
        
        return { value in
            workItem?.cancel()
            let newItem = DispatchWorkItem { function(value) }
            workItem = newItem
            queue.asyncAfter(deadline: .now() + limitDelay, execute: newItem)
        }
    }
}
