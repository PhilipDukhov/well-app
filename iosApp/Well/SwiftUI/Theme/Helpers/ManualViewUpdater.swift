//
// Created by Philip Dukhov on 1/18/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import Combine

final class ManualViewUpdater: ObservableObject {
    let objectWillChange = ObservableObjectPublisher()

    func update() {
        objectWillChange.send()
    }
}
