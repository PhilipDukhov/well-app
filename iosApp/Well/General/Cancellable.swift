//
//  Cancellable.swift
//  Well
//
//  Created by Philip Dukhov on 10/1/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation

protocol Cancellable {
    func cancel()
}

final class CancellableContainer: Cancellable {
    var cancellable: Cancellable {
        didSet {
            oldValue.cancel()
            if let container = cancellable as? CancellableContainer {
                cancellable = container.cancellable
            }
        }
    }
    
    init(cancellable: Cancellable) {
        self.cancellable = cancellable
    }
    
    func cancel() {
        cancellable.cancel()
    }
}
