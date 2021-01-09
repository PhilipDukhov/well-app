//
//  WebRtcManager.swift
//  Well
//
//  Created by Philip Dukhov on 12/27/20.
//  Copyright © 2020 Well. All rights reserved.
//

import Foundation
import SharedMobile
import WebRTC
import AVFoundation

private let rtcTrue = kRTCMediaConstraintsValueTrue

final class WebRtcManager: NSObject, WebRtcManagerI {
    private let listener: WebRtcManagerIListener
    private static let factory: RTCPeerConnectionFactory = {
        RTCInitializeSSL()
        return .init(
            encoderFactory: RTCDefaultVideoEncoderFactory(),
            decoderFactory: RTCDefaultVideoDecoderFactory()
        )
    }()
    private let peerConnection: RTCPeerConnection
    private let rtcAudioSession = RTCAudioSession.sharedInstance()
    private let audioQueue = DispatchQueue(label: "audio")
    private let rtcMedia = RTCMediaConstraints(mandatory: [
        kRTCMediaConstraintsOfferToReceiveAudio: rtcTrue,
        kRTCMediaConstraintsOfferToReceiveVideo: rtcTrue,
    ])

    private let videoCapturer: RTCVideoCapturer

    private let localAudioTrack: RTCAudioTrack
    private let localVideoTrack: RTCVideoTrack
    private let localDataChannel: RTCDataChannel
    private let localMediaStream: RTCMediaStream
    let localVideoContext: VideoViewContext

    private var remoteAudioTrack: RTCAudioTrack? {
        didSet {
            oldValue?.isEnabled = false
            remoteAudioTrack?.isEnabled = true
        }
    }
    private var remoteVideoTrack: RTCVideoTrack? {
        didSet {
            oldValue?.isEnabled = false
            remoteVideoTrack?.isEnabled = true
            listener.updateRemoveVideoContext(
                viewContext: remoteVideoTrack.map {
                    .init(videoTrack: $0)
                }
            )
        }
    }
    private var remoteDataChannel: RTCDataChannel?

    init(
        iceServers: [String],
        listener: WebRtcManagerIListener
    ) {
        try! AVAudioSession.sharedInstance()
            .setCategory(.playAndRecord)
        try! AVAudioSession.sharedInstance()
            .setActive(true)
        self.listener = listener
        let config = RTCConfiguration()
        config.iceServers = [RTCIceServer(urlStrings: iceServers)]

        // Unified plan is more superior than planB
        config.sdpSemantics = .unifiedPlan

        // gatherContinually will let WebRTC to listen to any network changes and send any new candidates to the other client
        config.continualGatheringPolicy = .gatherContinually

        let constraints = RTCMediaConstraints(
            optional: [
                "DtlsSrtpKeyAgreement": rtcTrue
            ]
        )
        peerConnection = Self.factory.peerConnection(
            with: config,
            constraints: constraints,
            delegate: nil
        )

        // MARK: - createMediaSenders

        // Audio

        let audioSource = Self.factory.audioSource(with: .init())
        localAudioTrack = Self.factory.audioTrack(with: audioSource, trackId: "audio0")
        // Video

        let videoSource = Self.factory.videoSource()

        #if TARGET_OS_SIMULATOR
        videoCapturer = RTCFileVideoCapturer(delegate: videoSource)
        #else
        videoCapturer = RTCCameraVideoCapturer(delegate: videoSource)
        #endif

        localVideoTrack = Self.factory.videoTrack(with: videoSource, trackId: "video0")
        localVideoContext = .init(videoTrack: localVideoTrack)
        // Data
        localDataChannel = peerConnection.dataChannel(
            forLabel: "WebRTCData",
            configuration: RTCDataChannelConfiguration()
        )!
        localMediaStream = Self.factory.mediaStream(withStreamId: "stream")
        localMediaStream.addAudioTrack(localAudioTrack)
        localMediaStream.addVideoTrack(localVideoTrack)
        super.init()
        localDataChannel.delegate = self
        peerConnection.delegate = self

        guard
            let capturer = videoCapturer as? RTCCameraVideoCapturer,
            let frontCamera = (RTCCameraVideoCapturer.captureDevices()
                .first {
                    $0.position == .front
                }),

            // choose highest res
            let format = (RTCCameraVideoCapturer.supportedFormats(for: frontCamera)
                .sorted {
                    $0.formatDescription.size.width
                    < $1.formatDescription.size.width
                }).last,

            // choose highest fps
            let fps = (format.videoSupportedFrameRateRanges.sorted {
                $0.maxFrameRate < $1.maxFrameRate
            }.last) else {
            return
        }

        capturer.startCapture(with: frontCamera,
            format: format,
            fps: Int(fps.maxFrameRate))
    }

    func close() {
        peerConnection.close()
    }

    func acceptAnswer(
        webRTCSessionDescriptor: String
    ) {
        peerConnection.setRemoteDescription(
            RTCSessionDescription(
                type: .answer,
                sdp: webRTCSessionDescriptor
            )
        ) { error in
            if let error = error {
                debugPrint(#function, error as Any)
            }
        }
    }

    func acceptCandidate(
        candidate: ServerModelsWebSocketMessage.Candidate
    ) {
        peerConnection.add(
            .init(
                sdp: candidate.sdp,
                sdpMLineIndex: candidate.sdpMLineIndex,
                sdpMid: candidate.sdpMid
            )
        )
    }

    func acceptOffer(
        webRTCSessionDescriptor: String
    ) {
        peerConnection.setRemoteDescription(
            .init(
                type: .offer,
                sdp: webRTCSessionDescriptor
            )
        ) { [weak self] error in
            if let error = error {
                print(#function, error)
                return
            }
            guard let self = self else {
                return
            }
            self.createOfferOrAnswer(
                create: self.peerConnection.answer
            ) { [weak self] in
                self?.listener.sendAnswer(webRTCSessionDescriptor: $0)
            }
        }
    }

    func sendOffer() {
        createOfferOrAnswer(
            create: peerConnection.offer
        ) { [weak self] in
            self?.listener.sendOffer(webRTCSessionDescriptor: $0)
        }
    }

    func createOfferOrAnswer(
        create: (RTCMediaConstraints, ((RTCSessionDescription?, Error?) -> Void)?) -> Void,
        completion: @escaping (String) -> Void
    ) {
        let streamIds = ["track"]
        peerConnection.add(localAudioTrack, streamIds: streamIds)
        peerConnection.add(localVideoTrack, streamIds: streamIds)
        create(
            rtcMedia
        ) { [weak self] description, error in
            if let description = description {
                self?.peerConnection.setLocalDescription(
                    description
                ) { error in
                    if let error = error {
                        print(#function, "setLocalDescription", error)
                        return
                    }
                    completion(description.sdp)
                }
            } else {
                debugPrint(#function, error as Any)
            }
        }
    }

    func sendData(
        data: UtilsData
    ) {
        localDataChannel.sendData(.init(data: data.data, isBinary: false))
    }
}

extension WebRtcManager: RTCPeerConnectionDelegate {
    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didChange stateChanged: RTCSignalingState
    ) {
        debugPrint(#function + ": \(stateChanged.rawValue)")
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didAdd stream: RTCMediaStream
    ) {
        remoteVideoTrack = stream.videoTracks.first
        remoteAudioTrack = stream.audioTracks.first
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didRemove stream: RTCMediaStream
    ) {
        remoteVideoTrack = nil
        remoteAudioTrack = nil
    }

    func peerConnectionShouldNegotiate(
        _ peerConnection: RTCPeerConnection
    ) {
        debugPrint(#function)
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didChange newState: RTCIceConnectionState
    ) {
        debugPrint(#function + ":RTCIceConnectionState \(newState.rawValue)")
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didChange newState: RTCIceGatheringState
    ) {
        debugPrint(#function + ":RTCIceGatheringState \(newState.rawValue)")
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didGenerate candidate: RTCIceCandidate
    ) {
        listener.addCandidate(candidate: candidate.toMessage())
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didRemove candidates: [RTCIceCandidate]
    ) {
        debugPrint("peerConnection did remove candidate(s)")
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didOpen dataChannel: RTCDataChannel
    ) {
        debugPrint("peerConnection did open data channel")
        remoteDataChannel = dataChannel
        dataChannel.delegate = self
    }
}

extension RTCIceCandidate {
    fileprivate func toMessage() -> ServerModelsWebSocketMessage.Candidate {
        .init(
            sdpMid: sdpMid ?? "",
            sdpMLineIndex: sdpMLineIndex,
            sdp: sdp
        )
    }
}

extension WebRtcManager: RTCDataChannelDelegate {
    func dataChannelDidChangeState(
        _ dataChannel: RTCDataChannel
    ) {
        debugPrint(#function, dataChannel.readyState.rawValue)
    }

    func dataChannel(
        _ dataChannel: RTCDataChannel,
        didReceiveMessageWith buffer: RTCDataBuffer
    ) {
        listener.receiveData(data: .init(data: buffer.data))
    }
}

extension WebRtcManager {
    private func setTrackEnabled<T: RTCMediaStreamTrack>(
        _ type: T.Type,
        isEnabled: Bool
    ) {
        peerConnection.transceivers
            .compactMap {
                $0.sender.track as? T
            }
            .forEach {
                $0.isEnabled = isEnabled
            }
    }
}

extension CMFormatDescription {
    fileprivate var size: CMVideoDimensions {
        CMVideoFormatDescriptionGetDimensions(self)
    }
}

extension RTCMediaConstraints {
    convenience init(
        mandatory: [String: String]? = nil,
        optional: [String: String]? = nil
    ) {
        self.init(mandatoryConstraints: mandatory, optionalConstraints: optional)
    }
}
