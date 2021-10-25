//
// Created by Phil on 22.10.2021.
// Copyright (c) 2021 Well. All rights reserved.
//

import Combine
import SharedMobile

final class ViewModelObservable<State, Msg, Eff, VM: ReducerViewModel<State, Msg, Eff>>: ObservableObject {
    private let childViewModel: VM

    @Published
    var state: State

    var closeable: AtomicCloseable?

    deinit {
        closeable?.close()
    }

    init(_ childViewModel: VM) {
        self.childViewModel = childViewModel
        state = childViewModel.state.value as! State
        
        closeable = childViewModel.state.onChange { [weak self] state in
            self?.state = state
        }
    }

    func listener(msg: Msg) {
        childViewModel.listener(msg: msg)
    }
}
