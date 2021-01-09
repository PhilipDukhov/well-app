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
typealias RTCVideoView = RTCMTLVideoView
#else
typealias RTCVideoView = RTCEAGLVideoView
#endif

final class UIVideoView: RTCVideoView {
    var videoTrack: RTCVideoTrack? {
        didSet {
            guard oldValue != videoTrack else {
                return
            }
            oldValue?.remove(self)
            videoTrack?.add(self)
        }
    }

    init() {
        super.init(frame: UIScreen.main.bounds)
        clipsToBounds = true
        #if arch(arm64)
        videoContentMode = .scaleAspectFill
        #endif
    }

    required init?(
        coder: NSCoder
    ) {
        fatalError("init(coder:) has not been implemented")
    }
}

struct VideoView: UIViewRepresentable {
    let context: VideoViewContext?

    func makeUIView(
        context: UIViewRepresentableContext<VideoView>
    ) -> UIVideoView {
        .init()
    }

    func updateUIView(
        _ uiView: UIVideoView,
        context: UIViewRepresentableContext<VideoView>
    ) {
        uiView.videoTrack = self.context?.videoTrack
    }

    func makeCoordinator() -> Coordinator {
        .init(parent: self)
    }

    static func dismantleUIView(
        _ uiView: UIVideoView,
        coordinator: Coordinator
    ) {
        uiView.videoTrack = nil
    }
    struct Coordinator {
        let parent: VideoView
    }
}
