//
//  DrawingContent.swift
//  Well
//
//  Created by Philip Dukhov on 2/6/21.
//  Copyright © 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct DrawingContent: View {
    typealias Listener = (DrawingFeature.Msg) -> Void
    let state: DrawingFeature.State
    let enabled: Bool
    let listener: Listener
    
    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                //                if let image = state.image {
                //                    Image(uiImage: image.uiImage)
                //                        .resizable()
                //                        .aspectRatio(contentMode: .fit)
                //                        .fillMaxSize()
                //                }
                drawer()
                    .onAppear {
                        listener(
                            DrawingFeature.MsgUpdateLocalViewSize(
                                size: geometry.size.toSize()
                            )
                        )
                    }
            }
        }
    }
    
    @ViewBuilder
    func drawer() -> some View {
        ZStack {
            ForEach(state.canvasPaths, id: \.self) { (path: SharedMobile.Path) in
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
                    if enabled {
                        listener(
                            DrawingFeature.MsgNewDragPoint(
                                point: value.location.toPoint()
                            )
                        )
                    }
                }
                .onEnded { _ in
                    if enabled {
                        listener(
                            DrawingFeature.MsgEndDrag()
                        )
                    }
                }
        )
    }
}
