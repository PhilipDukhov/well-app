//
// Created by Philip Dukhov on 1/19/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
struct Label: UIViewRepresentable, Equatable {
    typealias UIViewType = UILabel
    let text: String
    let style: TextStyle
    let textColor: UIColor

    func makeUIView(
        context: UIViewRepresentableContext<Self>
    ) -> UIViewType {
        UIViewType().apply {
            $0.textAlignment = .center
            $0.backgroundColor = .green
        }
    }

    func updateUIView(
        _ uiView: UIViewType,
        context: UIViewRepresentableContext<Self>
    ) {
        uiView.text = text
        uiView.font = UIFont.systemFont(ofSize: style.fontSize, weight: style.fontWeight.toUIWeight())
        uiView.textColor = textColor
        uiView.sizeToFit()
    }
}
