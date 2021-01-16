//
// Created by Philip Dukhov on 1/15/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI

struct ImageSharingBottomBar: View {
    let colors: [Color]
    let selected: Color
    let onSelected: (Color) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(colors.indices) { i in
                let color = colors[i]
                Circle()
                    .stroke(
                        selected == color ? Color.blue : .clear,
                        lineWidth: 2
                    )
                    .background(
                        OffsetCircle(offset: 2)

                            .fill(color)
                    )
                    .padding(2)
                    .onTapGesture {
                        onSelected(color)
                    }
            }
            WidthSelectionShape()
                .fill(Color.white)
                .frame(size: 20)
                .padding(2)
        }.frame(height: 51)
            .padding(2)
            .background(Color(white: 39 / 255).edgesIgnoringSafeArea(.all))
    }
}

struct WidthSelectionShape: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        let heights = [
            rect.height * 0.2,
            rect.height * 0.13,
            rect.height * 0.067,
        ]
        let separator = (rect.height - heights.reduce(0, +)) / CGFloat(heights.count - 1)
        path.addRects([
            CGRect(
                x: 0,
                y: 0,
                width: rect.width,
                height: heights[0]
            ),
            CGRect(
                x: 0,
                y: heights[0] + separator,
                width: rect.width,
                height: heights[1]
            ),
            CGRect(
                x: 0,
                y: rect.height - heights[2],
                width: rect.width,
                height: heights[2]
            )
        ])
        return path
    }
}
