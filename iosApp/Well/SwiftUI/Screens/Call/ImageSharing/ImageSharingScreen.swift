//
// Created by Philip Dukhov on 1/9/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct ImageSharingScreen: View {
    typealias Listener = (ImageSharingFeature.Msg) -> Void
    private let state: ImageSharingFeature.State
    private let listener: Listener

    init(
        state: ImageSharingFeature.State,
        listener: @escaping Listener
    ) {
        self.state = state
        self.listener = listener
    }

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                topPanel(geometry)
                drawingContent().fillMaxSize().clipped()
                bottomPanel(geometry)
            }
                .background(Color.black)
                .foregroundColor(.white)
                .edgesIgnoringSafeArea(.all)
                .statusBar(style: .lightContent)
        }
    }

    @ViewBuilder
    func topPanel(_ geometry: GeometryProxy) -> some View {
        ZStack(alignment: .center) {
            HStack {
                Control(systemName: "chevron.left", enabled: true) {
                    listener(ImageSharingFeature.MsgClose())
                }
                Spacer()
            }
            HStack {
                Control(systemName: "arrow.uturn.left", enabled: state.undoAvailable) {
                    listener(ImageSharingFeature.MsgUndo())
                }
                Control(systemName: "arrow.uturn.right", enabled: state.redoAvailable) {
                    listener(ImageSharingFeature.MsgRedo())
                }
            }
        }.padding(.top, geometry.safeAreaInsets.top)
    }

    @ViewBuilder
    func drawingContent() -> some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                if let image = state.image {
                    Image(uiImage: image.uiImage)
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                        .fillMaxSize()
                } else if state.role == .viewer {
                    Text("Waiting for an image")
                        .style(.h4)
                        .background(Color.green)
                }
                drawer()
                    .onAppear {
                        listener(
                            ImageSharingFeature.MsgUpdateLocalViewSize(
                                size: geometry.size.toSize()
                            )
                        )
                    }
                WidthSlider(
                    value: state.lineWidth,
                    range: ImageSharingFeature.StateCompanion().lineWidthRange.toClosedRange(),
                    color: state.currentColor.toColor()
                ) {
                    listener(
                        ImageSharingFeature.MsgUpdateLineWidth(lineWidth: $0)
                    )
                }.frame(width: geometry.size.width / 2, height: geometry.size.height / 2)
            }
        }
    }

    @ViewBuilder
    func bottomPanel(_ geometry: GeometryProxy) -> some View {
        let drawingColors = ImageSharingFeature.StateCompanion().drawingColors
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
                            listener(ImageSharingFeature.MsgUpdateColor(color: color))
                        }
                        .allowsHitTesting(!selected)
                }
            }
                .padding([.trailing, .leading, .top], padding)
                .padding(.bottom, padding + geometry.safeAreaInsets.bottom)
        }
            .background(Color.green)
    }

    @ViewBuilder
    func drawer() -> some View {
        ZStack {
            ForEach(state.canvasPaths, id: \.self) { (path: ServerModelsPath) in
                DrawShape(
                    points: path.points.map {
                        $0.toCGPoint()
                    },
                    touchTolerance: path.lineWidth.toCGFloat()
                )
                    .stroke(style: state.strokeStyle(lineWidth: path.lineWidth.toCGFloat()))
                    .foregroundColor(path.color.toColor())
                    .allowsHitTesting(false)
            }
        }.fillMaxSize().contentShape(Rectangle())
            .gesture(
                DragGesture()
                    .onChanged { value in
                        listener(
                            ImageSharingFeature.MsgNewDragPoint(
                                point: value.location.toServerModelsPoint()
                            )
                        )
                    }
                    .onEnded { _ in
                        listener(
                            ImageSharingFeature.MsgEndDrag()
                        )
                    }
            )
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
