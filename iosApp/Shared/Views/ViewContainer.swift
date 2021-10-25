//
// Created by Philip Dukhov on 1/18/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct ViewContainer<Content: View>: View {
    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        let view = content.frame(maxWidth: .infinity)
        if #available(iOS 14.0, *) {
            view
        } else {
            Button(action: {
            }, label: {
                view.animation(.none)
            }).disabled(true)
        }
    }
}
