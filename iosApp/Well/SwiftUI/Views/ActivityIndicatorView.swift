//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct ActivityIndicator: UIViewRepresentable {
    typealias UIViewType = UIActivityIndicatorView
    let style: UIActivityIndicatorView.Style = .medium

    func makeUIView(
        context: UIViewRepresentableContext<Self>
    ) -> UIViewType {
        UIActivityIndicatorView(style: style)
    }

    func updateUIView(
        _ uiView: UIViewType,
        context: UIViewRepresentableContext<Self>
    ) {
        uiView.startAnimating()
    }
}
