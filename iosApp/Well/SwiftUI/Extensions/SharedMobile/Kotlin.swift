//
// Created by Philip Dukhov on 1/19/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SharedMobile

extension KotlinClosedFloatingPointRange {
    func toClosedRange<Bound>() -> ClosedRange<Bound> where Bound: Comparable {
        (start as! Bound)...(endInclusive as! Bound)
    }
}
