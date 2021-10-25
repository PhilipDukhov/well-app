//
//  Flow.swift
//  Well
//
//  Created by Phil on 23.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile
import Combine

extension Kotlinx_coroutines_coreStateFlow {
    func onChange<T>(perform action: @escaping (T) -> Void) -> AtomicCloseable {
        FlowHelperKt.onChange(self) {
            action($0 as! T)
        }
    }
}
