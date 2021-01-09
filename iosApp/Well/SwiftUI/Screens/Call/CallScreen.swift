//
//  CallScreen.swift
//  Well
//
//  Created by Philip Dukhov on 12/29/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct DrawShape: Shape {

    var points: [CGPoint]

    // drawing is happening here
    func path(
        in rect: CGRect
    ) -> Path {
        var path = Path()
        guard let firstPoint = points.first else {
            return path
        }

        path.move(to: firstPoint)
        for pointIndex in 1..<points.count {
            path.addLine(to: points[pointIndex])
        }
        return path
    }
}

struct CallScreen: View {
    let state: CallFeature.State
    let listener: (CallFeature.Msg) -> Void

    var body: some View {
        ZStack {
            fullScreenVideoView(state.remoteVideoContext)
            GeometryReader { geometry in
                VStack {
                    if state.status != .ongoing {
                        callInfoView(geometry)
                    }
                    Spacer()
                    state.localVideoContext.map(smallVideoView)
                    HStack {
                        bottomButtons()
                    }
                } // VStack
                    .padding()
                    .frame(width: geometry.size.width)
            } // GeometryReader

            DrawShape(points: points)
                .stroke(lineWidth: 5) // here you put width of lines
                .foregroundColor(.blue)
        } // ZStack
            .foregroundColor(.white)
            .statusBar(style: .lightContent)
            .gesture(
                DragGesture()
                    .onChanged { value in
                        addNewPoint(value)
                    }
                    .onEnded { _ in
                        // here you perform what you need at the end
                    }
            )
    }

    private func addNewPoint(
        _ value: DragGesture.Value
    ) {
        // here you can make some calculations based on previous points
        points.append(value.location)
    }

    @State var points: [CGPoint] = []

    private func fullScreenVideoView(
        _ context: VideoViewContext?
    ) -> some View {
        VideoView(context: context)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(backgroundView())
            .edgesIgnoringSafeArea(.all)
    }

    @ViewBuilder
    private func smallVideoView(
        _ context: VideoViewContext
    ) -> some View {
        HStack {
            Spacer() // move video view to the right
            let width = UIScreen.main.bounds.width / 4
            VideoView(context: context)
                .frame(
                    width: width,
                    height: width * 1920 / 1080
                )
        }
    }

    @ViewBuilder
    private func callInfoView(
        _ geometry: GeometryProxy
    ) -> some View {
        ProfileImage(state.user)
            .frame(size: geometry.size.width * 0.6)
            .padding(.bottom, 40)
        Text(state.user.fullName)
            .style(.h4)
            .multilineTextAlignment(.center)
            .frame(maxWidth: geometry.size.width * 0.9)
            .padding(.bottom, 11)
        Text(state.status.stringRepresentation)
            .style(.body1)
    }

    @ViewBuilder
    private func bottomButtons() -> some View {
        Spacer()
        CallDownButton {
            listener(CallFeature.MsgEnd())
        }
        switch state.status {
        case .incoming:
            Spacer()
            Spacer()
            CallUpButton {
                listener(CallFeature.MsgAccept())
            }
        default:
            EmptyView()
        }
        Spacer()
    }

    @ViewBuilder
    func backgroundView() -> some View {
        Color(hex: 0x1B3D6D)
            .overlay(LinearGradient(
                gradient: Gradient(
                    stops: [
                        .init(color: Color(hex: 0x1B3D6D), location: 0.109375),
                        .init(color: Color(hex: 0x1BFFE4, alpha: 0.8), location: 0.984375),
                    ]
                ),
                startPoint: .init(x: 0, y: 0.339901478),
                endPoint: .init(x: 0.386666667, y: 0.910098522)
            ).opacity(0.5))
    }
}
