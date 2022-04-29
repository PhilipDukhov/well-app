//
//  InactiveOverlay.swift
//  Well
//
//  Created by Philip Dukhov on 2/15/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI

struct InactiveOverlay<Content: View>: View {
    let content: () -> Content

    init(
        @ViewBuilder content: @escaping () -> Content
    ) {
        self.content = content
    }

    static func withActivityIndicator() -> Self
    where Content == ProgressView<EmptyView, EmptyView>
    {
        Self.init(content: ProgressView.init)
    }

    init()
    where Content == EmptyView
    {
        self.content = EmptyView.init
    }

    var body: some View {
        ZStack(alignment: .center) {
            ColorConstants.InactiveOverlay.toColor()
                .fillMaxSize()
            content()
        }.fillMaxSize().edgesIgnoringSafeArea(.all)
    }
}
