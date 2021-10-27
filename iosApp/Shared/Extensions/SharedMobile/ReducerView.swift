//
//  ReducerView.swift
//  Well
//
//  Created by Phil on 22.10.2021.
//  Copyright © 2021 Well. All rights reserved.
//

import SharedMobile
import SwiftUI

struct ReducerView<State: NSObject, Msg, Eff, ScreenView: View>: View {
    @SwiftUI.State
    var state: State
    
    @ViewBuilder
    private let view: (State, @escaping (Msg) -> Void) -> ScreenView
    private let reducer: (Msg, State) -> KotlinPair<State, NSSet>
    private let effHandler: ((Eff) -> Void)?
    
    init(
        initial: State,
        reducer:@escaping (Msg, State) -> KotlinPair<State, NSSet>,
        @ViewBuilder view: @escaping (State, @escaping (Msg) -> Void) -> ScreenView,
        effHandler: @escaping (Eff) -> Void
    ) {
        state = initial
        self.reducer = reducer
        self.view = view
        self.effHandler = effHandler
    }
    
    init(
        initial: State,
        reducer:@escaping (Msg, State) -> KotlinPair<State, NSSet>,
        @ViewBuilder view: @escaping (State, @escaping (Msg) -> Void) -> ScreenView
    ) where Eff == Void {
        state = initial
        self.reducer = reducer
        self.view = view
        self.effHandler = nil
    }
    
    var body: some View {
        view(state) {
            let pair = reducer($0, state)
            state = pair.first!
            pair.second?.forEach { eff in
                effHandler?(eff as! Eff)
            }
        }
    }
}

struct ViewModelView<ScreenView: View, State, Msg, Eff, VM: ReducerViewModel<State, Msg, Eff>>: View {
    @StateObject
    var model: ViewModelObservable<State, Msg, Eff, VM>
    
    @ViewBuilder
    let view: (State, @escaping (Msg) -> Void) -> ScreenView
    
    init(
        _ viewModel: VM,
        view: @escaping (State, @escaping (Msg) -> Void) -> ScreenView
    ) {
        _model = .init(wrappedValue: .init(viewModel))
        self.view = view
    }
    
    var body: some View {
        view(model.state, model.listener)
    }
}
