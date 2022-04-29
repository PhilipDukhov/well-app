//
//  VideoView.swift
//  Well
//
//  Created by Philip Dukhov on 12/29/20.
//  Copyright © 2020 Well. All rights reserved.
//

import SwiftUI
import SharedMobile

#if !DEBUG || canImport(WebRTC)
import WebRTC

extension VideoViewContext {
    // hack to decrease build time
    var videoTrack: RTCVideoTrack {
        videoTrackAny as! RTCVideoTrack
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

#if arch(arm64)
final class UIVideoView: RTCMTLVideoView {
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
        videoContentMode = .scaleAspectFill
        clipsToBounds = true
    }

    required init?(
        coder: NSCoder
    ) {
        fatalError("init(coder:) has not been implemented")
    }
}
#else
final class UIVideoView: UIView {
    var videoTrack: RTCVideoTrack? {
        didSet {
            guard oldValue != videoTrack else {
                return
            }
            oldValue?.remove(videoView)
            videoTrack?.add(videoView)
        }
    }
    private let videoView = RTCEAGLVideoView()

    init() {
        super.init(frame: UIScreen.main.bounds)
        clipsToBounds = true
    }

    required init?(
        coder: NSCoder
    ) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        videoView.frame = bounds
    }
}
#endif
#else
struct VideoView: UIViewRepresentable {

	let context: VideoViewContext?

	func makeUIView(context: Context) -> UIView {
		UIView()
	}
	
	func updateUIView(_ uiView: UIView, context: Context) {}
}
#endif
