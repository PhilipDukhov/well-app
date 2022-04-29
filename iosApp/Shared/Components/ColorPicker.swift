//
//  ColorPicker.swift
//  Well
//
//  Created by Philip Dukhov on 12/29/20.
//  Copyright © 2020 Well. All rights reserved.

//import SwiftUI
//import Colorful
//
//struct ColorPicker: UIViewRepresentable {
//    typealias UIColorPicker = Colorful.ColorPicker
//    @Binding var color: Color
//
//    final class Coordinator {
//        let color: Binding<Color>
//
//        init(color: Binding<Color>) {
//            self.color = color
//        }
//
//        @objc func handleChange(
//            _ colorPicker: UIColorPicker
//        ) {
//            color.wrappedValue = .init(colorPicker.color)
//        }
//    }
//
//    func makeUIView(
//        context: UIViewRepresentableContext<ColorPicker>
//    ) -> UIColorPicker {
//        .init()
//    }
//
//    func updateUIView(
//        _ uiView: UIColorPicker,
//        context: UIViewRepresentableContext<ColorPicker>
//    ) {
//        uiView.set(
//            color: .init(color),
//            colorSpace: .extendedSRGB
//        )
//        uiView.addTarget(
//            context.coordinator,
//            action: #selector(Coordinator.handleChange(_:)),
//            for: .valueChanged
//        )
//    }
//
//    static func dismantleUIView(
//        _ uiView: UIColorPicker,
//        coordinator: Coordinator
//    ) {
//        uiView.removeTarget(
//            coordinator,
//            action: #selector(Coordinator.handleChange(_:)),
//            for: .valueChanged
//        )
//    }
//
//    func makeCoordinator() -> Coordinator {
//        .init(color: $color)
//    }
//}
