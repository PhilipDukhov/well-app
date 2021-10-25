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
    let onTap: () -> Void
    
    init(
        _ container: T,
        enabled: Bool = true,
        onTap: @escaping () -> Void
    ) {
        self.container = container
        self.enabled = enabled
        self.onTap = onTap
    }
    
    var body: some View {
        Button(action: onTap) {
            container
                .frame(minSize: controlMinSize)
                .contentShape(Rectangle())
                .opacity(enabled ? 1 : 0.4)
                .allowsHitTesting(enabled)
        }
    }
}

let controlMinSize: CGFloat = 45
