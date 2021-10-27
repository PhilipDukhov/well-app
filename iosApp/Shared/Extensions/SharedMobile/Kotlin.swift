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

extension DayOfWeek: Identifiable {
    public var id: Int32 { ordinal }
}

//extension KotlinThrowable {
//    func asError() -> Error {
//        do {
//            try TestKt.rethrow(exception: self)
//        } catch {
//            return error
//        }
//        fatalError("should not reach here")
//    }
//}
