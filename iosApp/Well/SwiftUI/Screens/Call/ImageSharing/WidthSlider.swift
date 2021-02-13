//
// Created by Philip Dukhov on 1/20/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct WidthSlider: View {
    let value: Float
    let range: ClosedRange<Float>
    let color: Color
    let valueChanged: (Float) -> Void

    @State private var dragging = false

    private let thumbSize: CGFloat = 16
    private let sliderWidth: CGFloat = 30
    private let padding: CGFloat

    init(value: Float,
         range: ClosedRange<Float>,
         color: Color,
         valueChanged: @escaping (Float) -> Void
    ) {
        self.value = value
        self.range = range
        self.color = color
        self.valueChanged = valueChanged
        padding = (controlMinSize - sliderWidth / 2) / 2
    }

    private func valueToOffset(_ value: Float, _ height: CGFloat) -> CGFloat {
        padding + (height - thumbSize - padding * 2) * (1 - ((value - range.lowerBound) / (range.upperBound - range.lowerBound)).toCGFloat())
    }

    private func offsetToValue(_ offset: CGFloat, _ height: CGFloat) -> Float {
        (1 - (offset - padding) / (height - thumbSize - padding * 2)).toFloat() * (range.upperBound - range.lowerBound) + range.lowerBound
    }

    var body: some View {
        GeometryReader { geometry in
            let thumbOffset = valueToOffset(value, geometry.size.height)
            let thumbCenter = thumbOffset + thumbSize / 2
            let sampleSize = dragging ? value.toCGFloat() : 0
            HStack(alignment: .top) {
                ZStack(alignment: .top) {
                    Quadrangle(
                        topWidth: dragging ? sliderWidth / 1.77 : 2,
                        bottomWidth: dragging ? 0 : 1
                    ).foregroundColor(.white)
                        .opacity(0.65)
                        .padding(padding)
                        .animation(.default)
                    Circle()
                        .foregroundColor(.white)
                        .shadow(radius: 2)
                        .frame(size: thumbSize)
                        .scaledToFill()
                        .offset(y: thumbOffset)
                }.frame(width: sliderWidth) // ZStack
                    .fillMaxHeight()
                    .contentShape(Rectangle())
                    .gesture(
                        DragGesture(minimumDistance: 0)
                            .onChanged { value in
                                dragging = true
                                valueChanged(
                                    min(
                                        range.upperBound,
                                        max(
                                            range.lowerBound,
                                            offsetToValue(value.location.y, geometry.size.height)
                                        )
                                    )
                                )
                            }
                            .onEnded { _ in
                                print("DragGesture ended")
                                dragging = false
                            }
                    )

                Spacer()
                Circle()
                    .foregroundColor(color)
                    .frame(size: sampleSize)
                    .scaledToFill()
                    .offset(
                        x: -(range.upperBound - value).toCGFloat(),
                        y: thumbCenter - sampleSize / 2
                    )
            } // HStack
        } // GeometryReader
    }
}

private struct Quadrangle: Shape {
    private(set) var topWidth: CGFloat
    private(set) var bottomWidth: CGFloat

    var animatableData: AnimatablePair<CGFloat, CGFloat> {
        get {
            .init(topWidth, bottomWidth)
        }
        set {
            topWidth = newValue.first
            bottomWidth = newValue.second
        }
    }

    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX - bottomWidth / 2, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.midX + bottomWidth / 2, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.midX + topWidth / 2, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.midX - topWidth / 2, y: rect.minY))
        return path
    }
}
