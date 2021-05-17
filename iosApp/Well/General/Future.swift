//
//  Future.swift
//  Well
//
//  Created by Philip Dukhov on 9/30/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

final class Future<Value>: Hashable, Cancellable {
    typealias Promise = (Value) -> Void
    
    private let createdDate = Date()
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(createdDate)
    }
    
    init() {
    }
    
    init(value: Value) {
        self.complete(with: value)
    }
    
    init(work: (@escaping Promise) -> Void) {
        work(self.complete(with:))
    }
    
    private var value: Value?
    private var cancelled = false
    
    func complete(with value: Value) {
        guard self.value == nil, !cancelled else { return }
        
        self.value = value
        
        for callback in self.callbacks {
            callback(value)
        }
        
        self.callbacks = []
    }
    
    private var callbacks: [Promise] = []
    
    @discardableResult func onComplete(_ callback: @escaping Promise) -> Future {
        if let value = self.value {
            callback(value)
        } else {
            self.callbacks.append(callback)
        }
        
        return self
    }
    
    private var cancelCallbacks: [() -> Void] = []
    
    @discardableResult func onCancel(_ callback: @escaping () -> Void) -> Future {
        if value == nil, !cancelled {
            cancelCallbacks.append(callback)
        }
        
        return self
    }
    
    func cancel() {
        cancelled = true
        cancelCallbacks.forEach { $0() }
        cancelCallbacks = []
    }
}

extension Future: Equatable {
    static func == (lhs: Future<Value>, rhs: Future<Value>) -> Bool {
        return lhs === rhs
    }
}

extension Future {
    func map<NewValue>(_ transform: @escaping (Value) -> NewValue) -> Future<NewValue> {
        return (Future<NewValue> { promise in
            self.onComplete { value in
                promise(transform(value))
            }
        })
        .onCancel { self.cancel() }
    }
    
    func map<NewValue>(_ transform: @escaping (Value) -> Future<NewValue>) -> Future<NewValue> {
        let responseFuture = Future<NewValue> {_ in}
            .onCancel { self.cancel() }
        
        self.onComplete { value in
            let newFuture = transform(value)
            newFuture.onComplete { newValue in
                responseFuture.complete(with: newValue)
            }
            self.onCancel { newFuture.cancel() }
        }
        
        return responseFuture
    }
    
    func queue(_ queue: DispatchQueue) -> Future<Value> {
        return Future<Value> { promise in
            self.onComplete { value in
                queue.async {
                    promise(value)
                }
            }
        }
        .onCancel { self.cancel() }
    }
    
    func after(_ queue: DispatchQueue, time: DispatchTime) -> Future<Value> {
        return Future<Value> { promise in
            self.onComplete { value in
                queue.asyncAfter(deadline: time) {
                    promise(value)
                }
            }
        }
        .onCancel { self.cancel() }
    }
    
    var empty: Future<Void> { map { _ in Future<Void>(value: ()) }}
}

extension Future {
    static var empty: Future<Void> { return Future<Void>(value: ()) }
    
    func output(to property: Property<Value>) {
        self.onComplete { property.value = $0 }
    }
}
