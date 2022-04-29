//
//  Control.swift
//  Well
//
//  Created by Philip Dukhov on 2/12/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct Control<T>: View where T: View {
    let container: T
    let enabled: Bool
    let action: () -> Void
    
    init(
        _ container: T,
        enabled: Bool = true,
        action: @escaping () -> Void
    ) {
        self.container = container
        self.enabled = enabled
        self.action = action
    }

    init(
        action: @escaping () -> Void,
        enabled: Bool = true,
        @ViewBuilder _ container: () -> T
    ) {
        self.container = container()
        self.enabled = enabled
        self.action = action
    }
    
    var body: some View {
        Button(action: action) {
            container
                .frame(minSize: controlMinSize)
                .contentShape(Rectangle())
                .opacity(enabled ? 1 : 0.4)
        }
        .disabled(!enabled)
    }
}

let controlMinSize: CGFloat = 45
