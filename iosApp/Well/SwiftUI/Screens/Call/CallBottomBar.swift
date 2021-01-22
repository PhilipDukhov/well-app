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
            gradientBackground()
            HStack {
                ToggleStillButton(
                    systemImage: "pencil",
                    size: .standard,
                    selected: false
                ) {
                    listener(CallFeature.MsgStartImageSharing())
                }.frame(maxWidth: .infinity)
                // MARK: - micEnabled
                ToggleStillButton(
                    systemImage: "mic.slash.fill",
                    size: .standard,
                    selected: state.deviceState.micEnabled
                ) {
                    listener(state.deviceState.toggleMicMsg())
                }.frame(maxWidth: .infinity)
                // MARK: - Counter label
                CallTimePassedLabel(
                    info: state.callStartedDateInfo,
                    visible: state.status == .ongoing
                )
                // MARK: - audioSpeakerEnabled
                ToggleStillButton(
                    systemImage: "video.slash.fill",
                    size: .standard,
                    selected: state.deviceState.cameraEnabled
                ) {
                    listener(state.deviceState.toggleCameraMsg())
                }.frame(maxWidth: .infinity)
                // MARK: - audioSpeakerEnabled
                ToggleStillButton(
                    systemImage: "speaker.1.fill",
                    size: .standard,
                    selected: state.deviceState.audioSpeakerEnabled
                ) {
                    listener(state.deviceState.toggleAudioSpeakerMsg())
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

    @ViewBuilder
    private func gradientBackground() -> some View {
        Color(hex: 0x1B3D6D).overlay(LinearGradient(
            gradient: Gradient(
                stops: [
                    .init(color: Color(hex: 0x1BFFE4, alpha: 0.8), location: 0.109375),
                    .init(color: Color(hex: 0x1B3D6D), location: 0.984375),
                ]
            ),
            startPoint: .init(x: 0.762648, y: -0.376472566),
            endPoint: .init(x: 0.812653333, y: 0.803190265)
        ).opacity(0.5))
    }

    private let radius = ViewConstants.CallScreen().CallButtonRadius.toCGFloat()
    private let padding = ViewConstants.CallScreenBottomBar().CallButtonPadding.toCGFloat()
    private let offset = ViewConstants.CallScreenBottomBar().CallButtonOffset.toCGFloat()
    static let baseHeight = ViewConstants.CallScreenBottomBar().Height.toCGFloat()
}

private struct CallBottomShape: Shape {
    let radius: CGFloat
    let offset: CGFloat

    func path(in rect: CGRect) -> Path {
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
