//
//  CallScreen.swift
//  Well
//
//  Created by Philip Dukhov on 12/29/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

struct CallScreen: View {
    let state: CallFeature.State
    let listener: (CallFeature.Msg) -> Void

    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .top) {
                backgroundView()
                    .fillMaxSize()
                callInfoView(geometry)
                fullScreenVideoView(state.remoteVideoContext)
                VStack(spacing: 0) {
                    Spacer()
                    let ongoing = state.status == .ongoing
                    let bottomBarHeight = CallBottomBar.baseHeight + geometry.safeAreaInsets.bottom
                    let callButtonsOffset = ongoing ? callButtonRadius - callButtonOffset : CallBottomBar.baseHeight
                    if ongoing || state.status == .connecting,
                       let localVideoContext = state.localVideoContext {
                        smallVideoView(localVideoContext, geometry)
                            .offset(y: callButtonsOffset)
                    }
                    callButtons()
                        .offset(y: callButtonsOffset)
                        .padding(.all, ongoing ? 0 : nil)
                    CallBottomBar(
                        state: state,
                        listener: listener,
                        geometry: geometry
                    ).frame(height: bottomBarHeight)
                        .offset(y: ongoing ? 0 : bottomBarHeight)
                } // VStack
                    .frame(maxWidth: .infinity)
            } // ZStack
                .edgesIgnoringSafeArea(.all)
        } // GeometryReader
            .foregroundColor(.white)
            .statusBar(style: .lightContent)
            .animation(.default)
    }

    private func fullScreenVideoView(
        _ context: VideoViewContext?
    ) -> some View {
        VideoView(context: context)
            .fillMaxSize()
    }

    @ViewBuilder
    private func smallVideoView(
        _ context: VideoViewContext?,
        _ geometry: GeometryProxy
    ) -> some View {
        HStack {
            Spacer() // move video view to the right
            let width = geometry.size.width / 3
            VStack {
                VideoView(context: context)
                    .frame(
                        width: width,
                        height: width * 1920 / 1080
                    )
                    .background(Color.green)
                Image(systemName: "camera.rotate.fill")
                    .font(.system(size: 30))
                    .frame(size: 45)
                    .onTapGesture {
                        listener(state.deviceState.toggleIsFrontCameraMsg())
                    }
            }
        }.padding()
    }

    @ViewBuilder
    private func callInfoView(
        _ geometry: GeometryProxy
    ) -> some View {
        let ongoing = state.status == .ongoing
        let profileImageSize = ongoing ? geometry.size : CGSize(size: geometry.size.width * 0.6)
        let profileImageOffset: CGFloat = ongoing ? 0 : (20 + geometry.safeAreaInsets.top)
        let textInfoOffset = ongoing ? 0 : profileImageOffset + profileImageSize.height

        ProfileImage(state.user, clipCircle: !ongoing)
            .clipped()
            .frame(size: profileImageSize)
            .offset(y: profileImageOffset)
            .edgesIgnoringSafeArea([])
        VStack {
            if ongoing {
                Spacer()
            }
            Text(state.user.fullName)
                .style(.h4)
                .multilineTextAlignment(.center)
                .frame(maxWidth: geometry.size.width * 0.9)
                .padding(.bottom, 11)
                .offset(y: ongoing ? -geometry.size.height / 4 : 0)
            if ongoing {
                Spacer()
            } else {
//            Label(
//                text: state.status.stringRepresentation,
//                style: .body1,
//                textColor: .white
//            )
                ViewContainer {
                    Text(state.status.stringRepresentation)
                        .frame(maxWidth: geometry.size.width * 0.9)
                }.frame(maxWidth: .infinity)
            }
        }.offset(y: textInfoOffset)
    }

    @ViewBuilder
    private func callButtons() -> some View {
        HStack {
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
                startPoint: .init(x: 1.1673333333333331, y: 0.8408037094281299),
                endPoint: .init(x: -0.3820000000000002, y: -0.3898763523956724)
            ).opacity(0.5))
    }

    private let callButtonOffset = ViewConstants.CallScreenBottomBar().CallButtonOffset.toCGFloat()
    private let callButtonRadius = ViewConstants.CallScreen().CallButtonRadius.toCGFloat()
}
