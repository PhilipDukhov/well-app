//
// Created by Philip Dukhov on 1/9/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct DrawingPanel: View {
    typealias Listener = (DrawingFeature.Msg) -> Void
    private let geometry: GeometryProxy
    private let state: DrawingFeature.State
    private let listener: Listener
    private let back: () -> Void

    init(
        geometry: GeometryProxy,
        state: DrawingFeature.State,
        listener: @escaping Listener,
        back: @escaping () -> Void
    ) {
        self.geometry = geometry
        self.state = state
        self.listener = listener
        self.back = back
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            topPanel()
                .background(Color.black)
            Spacer()
            WidthSlider(
                value: state.lineWidth,
                range: DrawingFeature.StateCompanion().lineWidthRange.toClosedRange(),
                color: state.currentColor.toColor()
            ) {
                listener(
                    DrawingFeature.MsgUpdateLineWidth(lineWidth: $0)
                )
            }.frame(width: geometry.size.width / 2, height: geometry.size.height / 2)
            Spacer()
            bottomPanel()
        }
        .foregroundColor(.white)
        .edgesIgnoringSafeArea(.all)
    }

    @ViewBuilder
    func topPanel() -> some View {
        ZStack(alignment: .center) {
            HStack {
                Control(systemName: "chevron.left", enabled: true, onTap: back)
                Spacer()
            }
            HStack {
                Control(systemName: "arrow.uturn.left", enabled: state.undoAvailable) {
                    listener(DrawingFeature.MsgUndo())
                }
                Control(systemName: "arrow.uturn.right", enabled: state.redoAvailable) {
                    listener(DrawingFeature.MsgRedo())
                }
            }
        }.padding(.top, geometry.safeAreaInsets.top)
    }

    @ViewBuilder
    func bottomPanel() -> some View {
        let drawingColors = DrawingFeature.StateCompanion().drawingColors
        let spacing: CGFloat = 5
        let padding: CGFloat = 8
        Group {
            HStack(spacing: spacing) {
                ForEach(drawingColors, id: \.self) { color in
                    let selected = state.currentColor == color
                    Circle()
                        .stroke(lineWidth: 2)
                        .foregroundColor(selected ? .blue : .clear)
                        .scaledToFit()
                        .overlay(
                            Circle()
                                .foregroundColor(color.toColor())
                                .padding(3)
                        )
                        .onTapGesture {
                            listener(DrawingFeature.MsgUpdateColor(color: color))
                        }
                        .allowsHitTesting(!selected)
                }
            }
                .padding([.trailing, .leading, .top], padding)
                .padding(.bottom, padding + geometry.safeAreaInsets.bottom)
        }
        .background(ColorConstants.MineShaft.toColor())
    }
}

private struct Control: View {
    let systemName: String
    let enabled: Bool
    let onTap: () -> Void

    var body: some View {
        Image(systemName: systemName)
            .frame(size: 45)
            .onTapGesture(perform: onTap)
            .opacity(enabled ? 1 : 0.4)
            .allowsHitTesting(enabled)
    }
}
