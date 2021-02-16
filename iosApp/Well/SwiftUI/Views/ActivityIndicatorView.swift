//
// Created by Philip Dukhov on 12/31/20.
// Copyright (c) 2020 Well. All rights reserved.
//

import SwiftUI

struct ActivityIndicator: UIViewRepresentable {
    typealias UIViewType = UIActivityIndicatorView
    let style: UIActivityIndicatorView.Style
    let color: UIColor
    
    internal init(style: UIActivityIndicatorView.Style = .medium, color: UIColor = .white) {
        self.style = style
        self.color = color
    }

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
        uiView.color = color
    }
}
