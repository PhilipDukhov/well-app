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
        ZStack {
//            (state.remoteVideoContext != nil ? state.remoteVideoContext : state.localVideoContext)
            state.remoteVideoContext
                .map(VideoView.init)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.gray)
                .edgesIgnoringSafeArea(.all)
            GeometryReader { geometry in
                VStack {
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
                    Spacer()
//                    (state.remoteVideoContext != nil ? state.localVideoContext : nil)
                    state.localVideoContext
                        .map(smallViewView)
                    HStack {
                        bottomButtons()
                    }
                }.padding().frame(width: geometry.size.width) //VStack
            } // GeometryReader
        }.foregroundColor(.white) //ZStack
    }

    @ViewBuilder
    private func smallViewView(
        context: VideoViewContext
    ) -> some View {
        HStack {
            Spacer() // move video view to the right
            VideoView(model: context)
                .frame(width: 112, height: 200)
        }
        Spacer()
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
}
