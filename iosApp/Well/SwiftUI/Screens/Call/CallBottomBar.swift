//
// Created by Philip Dukhov on 1/17/21.
// Copyright (c) 2021 Well. All rights reserved.
//

import SwiftUI
import SharedMobile
import Combine

struct CallBottomBar: View {
    let state: CallFeature.State
    let listener: (CallFeature.Msg) -> Void
    let geometry: GeometryProxy

    var body: some View {
        ZStack(alignment: .top) {
            GradientKMM(gradient: .callBottomBar)
            HStack {
                printUI("state update", state.localDeviceState)
                ToggleStillButton(
                    systemImage: "pencil",
                    size: .standard,
                    selected: false
                ).onTapGesture {
                    listener(CallFeature.MsgInitializeDrawing())
                }.frame(maxWidth: .infinity)
                // MARK: - micEnabled
                ToggleStillButton(
                    systemImage: "mic.slash.fill",
                    size: .standard,
                    selected: !state.localDeviceState.micEnabled
                ).onTapGesture {
                    listener(state.localDeviceState.toggleMicMsg())
                }.frame(maxWidth: .infinity)
                // MARK: - Counter label
                CallTimePassedLabel(
                    info: state.callStartedDateInfo,
                    visible: state.status == .ongoing
                )
                switch state.viewPoint {
                case .both:
                    // MARK: - audioSpeakerEnabled
                    ToggleStillButton(
                        systemImage: "video.slash.fill",
                        size: .standard,
                        selected: !state.localDeviceState.cameraEnabled
                    ).onTapGesture {
                        listener(state.localDeviceState.toggleCameraMsg())
                    }.frame(maxWidth: .infinity)
                    
                case .mine, .partner:
                    // MARK: - reset view poitn
                    ToggleStillButton(
                        systemImage: "stop.circle",
                        size: .standard,
                        selected: false
                    ).onTapGesture {
                        listener(CallFeature.MsgLocalUpdateViewPoint(viewPoint: .both))
                    }.frame(maxWidth: .infinity)
                    
                default:
                    fatalError()
                }
                // MARK: - audioSpeakerEnabled
                ToggleStillButton(
                    systemImage: "speaker.1.fill",
                    size: .standard,
                    selected: state.localDeviceState.audioSpeakerEnabled
                ).onTapGesture {
                    listener(state.localDeviceState.toggleAudioSpeakerMsg())
                }.frame(maxWidth: .infinity)
            }.frame(height: Self.baseHeight)
        }.frame(maxWidth: .infinity)
            .clipShape(
                CallBottomShape(
                    radius: radius + padding,
                    offset: offset
                )
            )
            .clipped()
    }

    private let radius = ViewConstants.CallScreen().CallButtonRadius.toCGFloat()
    private let padding = ViewConstants.CallScreenBottomBar().CallButtonPadding.toCGFloat()
    private let offset = ViewConstants.CallScreenBottomBar().CallButtonOffset.toCGFloat()
    static let baseHeight = ViewConstants.CallScreenBottomBar().Height.toCGFloat()
}

private struct CallBottomShape: Shape {
    let radius: CGFloat
    let offset: CGFloat

    func path(in rect: CGRect) -> SwiftUI.Path {
        Path { path in
            path.addRect(rect)
            path.addPath(.init(UIBezierPath(
                arcCenter: .init(x: rect.midX, y: rect.minY - offset),
                radius: radius,
                startAngle: 0,
                endAngle: .pi,
                clockwise: true
            ).reversing().cgPath))
        }
    }
}

private struct CallTimePassedLabel: View {
    let info: CallFeature.StateCallStartedDateInfo?
    let visible: Bool
    private let timer = Timer.publish(every: 1, on: .main, in: .common)
        .autoconnect()
    @ObservedObject
    private var manualViewUpdater = ManualViewUpdater()

    var body: some View {
        ViewContainer {
            if let info = info {
//                Label(
//                    text: info.secondsPassedFormatted,
//                    style: .body1,
//                    textColor: .white
//                )
                Text(info.secondsPassedFormatted).onReceive(timer) { _ in
                    if visible {
                        manualViewUpdater.update()
                    }
                }
            }
        }.frame(maxWidth: .infinity)
    }
}
