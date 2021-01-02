//
//  VideoView.swift
//  Well
//
//  Created by Philip Dukhov on 12/29/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import WebRTC
import SharedMobile

#if arch(arm64)
typealias UIVideoView = RTCMTLVideoView
#else
typealias UIVideoView = RTCEAGLVideoView
#endif

struct VideoView: UIViewRepresentable {
    struct Coordinator {
        let parent: VideoView
    }

    let model: VideoViewContext

    func makeUIView(
        context: UIViewRepresentableContext<VideoView>
    ) -> UIVideoView {
        UIVideoView().apply { videoView in
            #if arch(arm64)
            videoView.videoContentMode = .scaleAspectFill
            #endif
            videoView.frame = UIScreen.main.bounds
        }
    }

    func updateUIView(
        _ uiView: UIVideoView,
        context: UIViewRepresentableContext<VideoView>
    ) {
        model.videoTrack.add(uiView)
    }

    func makeCoordinator() -> Coordinator {
        .init(parent: self)
    }

    static func dismantleUIView(
        _ uiView: RTCMTLVideoView,
        coordinator: Coordinator
    ) {
        coordinator.parent.model.videoTrack.remove(uiView)
    }
}
