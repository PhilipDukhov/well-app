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
    private var deviceState = LocalDeviceState.Companion().default_
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
    private var cameraVideoCapturer: RTCCameraVideoCapturer? {
        videoCapturer as? RTCCameraVideoCapturer
    }

    private let localAudioTrack: RTCAudioTrack
    private let localVideoTrack: RTCVideoTrack
    private let localDataChannel: RTCDataChannel
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
                    .init(videoTrackAny: $0)
                }
            )
        }
    }
    private var remoteDataChannel: RTCDataChannel?
    var manyCamerasAvailable: Bool {
        Set(
            RTCCameraVideoCapturer.captureDevices()
                .map { $0.position == .front }
        ).count == 2
    }
    private let streamIds = ["track"]

    init(
        iceServers: [String],
        listener: WebRtcManagerIListener
    ) {
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
        localAudioTrack.isEnabled = deviceState.micEnabled
        // Video

        let videoSource = Self.factory.videoSource()

        #if TARGET_OS_SIMULATOR
        videoCapturer = RTCFileVideoCapturer(delegate: videoSource)
        #else
        videoCapturer = RTCCameraVideoCapturer(delegate: videoSource)
        #endif

        localVideoTrack = Self.factory.videoTrack(with: videoSource, trackId: "video0")
        localVideoContext = .init(videoTrackAny: localVideoTrack)
        // Data
        localDataChannel = peerConnection.dataChannel(
            forLabel: "WebRTCData",
            configuration: RTCDataChannelConfiguration()
        )!
        super.init()
        rtcAudioSession.add(self)
        localDataChannel.delegate = self
        peerConnection.delegate = self

        startCapture(position: deviceState.isFrontCamera ? .front : .back)
        setSpeakerEnabled(enabled: deviceState.audioSpeakerEnabled)
    }

    func close() {
        rtcAudioSession.remove(self)
        peerConnection.close()
        [localAudioTrack,
         localVideoTrack,
         remoteAudioTrack,
         remoteVideoTrack,
        ].forEach {
            $0?.isEnabled = false
        }
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
                Napier.w(#function, error as Any)
            }
        }
    }

    func acceptCandidate(
        candidate: WebSocketMsg.CallCandidate
    ) {
        peerConnection.add(
            .init(
                sdp: candidate.sdp,
                sdpMLineIndex: candidate.sdpMLineIndex,
                sdpMid: candidate.sdpMid
            )
        )
//        ) { error in
//            if let error = error {
//                Napier.w(#function, error)
//            }
//        }
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
                Napier.w(#function, error)
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

    private var localVideoTrackSender: RTCRtpSender?
    func createOfferOrAnswer(
//        create: (RTCMediaConstraints, @escaping RTCCreateSessionDescriptionCompletionHandler) -> Void,
        create: (RTCMediaConstraints, ((RTCSessionDescription?, Error?) -> Void)?) -> Void,
        completion: @escaping (String) -> Void
    ) {
        peerConnection.add(localAudioTrack, streamIds: streamIds)
        localVideoTrackSender = peerConnection.add(localVideoTrack, streamIds: streamIds)
        create(
            rtcMedia
        ) { [weak self] description, error in
            if let description = description {
                self?.peerConnection.setLocalDescription(
                    description
                ) { error in
                    if let error = error {
                        Napier.w(#function, "setLocalDescription", error)
                        return
                    }
                    completion(description.sdp)
                }
            } else {
                Napier.e(#function, error as Any)
            }
        }
    }

    func sendData(
        data: KotlinByteArray
    ) {
        localDataChannel.sendData(.init(data: data.toNSData(), isBinary: false))
    }

    func syncDeviceState(deviceState: LocalDeviceState) {
        var startCaptureNeeded = false
        if self.deviceState.isFrontCamera != deviceState.isFrontCamera {
            startCaptureNeeded = true
            startCapture(position: deviceState.isFrontCamera ? .front : .back)
        }
        if self.deviceState.micEnabled != deviceState.micEnabled {
            localAudioTrack.isEnabled = deviceState.micEnabled
        }
        if self.deviceState.audioSpeakerEnabled != deviceState.audioSpeakerEnabled {
            setSpeakerEnabled(enabled: deviceState.audioSpeakerEnabled)
        }
        if self.deviceState.cameraEnabled != deviceState.cameraEnabled {
            if deviceState.cameraEnabled {
//                localVideoTrackSender = peerConnection.add(localAudioTrack, streamIds: streamIds)
            } else {
//                localVideoTrackSender.map {
//                    peerConnection.removeTrack($0)
//                }
                cameraVideoCapturer?.stopCapture()
            }
            startCaptureNeeded = deviceState.cameraEnabled
            localVideoTrack.isEnabled = deviceState.cameraEnabled
//            peerConnection.add(localVideoTrack, streamIds: streamIds)
        }
        if startCaptureNeeded {
            startCapture(position: deviceState.isFrontCamera ? .front : .back)
        }
        self.deviceState = deviceState
    }

    private func startCapture(position: AVCaptureDevice.Position) {
        guard
            let capturer = cameraVideoCapturer,
            let camera = (RTCCameraVideoCapturer.captureDevices()
                .first {
                    $0.position == position
                }),
            case let formats = ((RTCCameraVideoCapturer.supportedFormats(for: camera)
                .sorted {
                    $0.formatDescription.size.width < $1.formatDescription.size.width
                })
                .filter {
                !$0.description.contains("x420")
            }),
            let format = (formats.first { format in
                let dimensions = format.formatDescription.dimensions
                return dimensions.width == 1280 && dimensions.height == 720 &&
                       format.videoFieldOfView > 60 &&
                       format.videoSupportedFrameRateRanges.last?.maxFrameRate == 30
            } ?? formats.first { format in
                let dimensions = format.formatDescription.dimensions
                return dimensions.width >= 1000 && dimensions.height >= 1000
            } ?? formats.last) ,
            let maxFps = (format.videoSupportedFrameRateRanges
                .map {
                    $0.maxFrameRate
                }
                .max()
            )
            else {
            return
        }

        capturer.startCapture(
            with: camera,
            format: format,
            fps: min(Int(maxFps), 30)
        )
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
            self?.listener.updateCaptureDimensions(
                dimensions: Size(
                    width: format.formatDescription.dimensions.height,
                    height: format.formatDescription.dimensions.width
                )
            )
        }
    }

    private func setSpeakerEnabled(enabled: Bool) {
        audioQueue.async { [weak self] in
            guard let self = self else {
                return
            }

            self.rtcAudioSession.lockForConfiguration()
            do {
                try self.rtcAudioSession.overrideOutputAudioPort(enabled ? .speaker : .none)
            } catch let error {
                Napier.w("Couldn't force audio to speaker: \(error)")
            }
            self.rtcAudioSession.unlockForConfiguration()
            
            Napier.i(#function, enabled)
        }
    }
}

extension WebRtcManager: RTCPeerConnectionDelegate {
    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didChange stateChanged: RTCSignalingState
    ) {
        Napier.i(#function, stateChanged.rawValue)
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
        Napier.i(#function)
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didChange newState: RTCIceConnectionState
    ) {
        Napier.i(#function + ":RTCIceConnectionState \(newState.rawValue)")
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didChange newState: RTCIceGatheringState
    ) {
        Napier.i(#function + ":RTCIceGatheringState \(newState.rawValue)")
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
        Napier.i("peerConnection did remove candidate(s)")
    }

    func peerConnection(
        _ peerConnection: RTCPeerConnection,
        didOpen dataChannel: RTCDataChannel
    ) {
        Napier.i("peerConnection did open data channel")
        remoteDataChannel = dataChannel
        dataChannel.delegate = self
    }
}

extension RTCIceCandidate {
    fileprivate func toMessage() -> WebSocketMsg.CallCandidate {
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
        listener.dataChannelStateChanged(state: dataChannel.readyState.toDataChannelState())
    }

    func dataChannel(
        _ dataChannel: RTCDataChannel,
        didReceiveMessageWith buffer: RTCDataBuffer
    ) {
        listener.receiveData(data: NSData_byteArrayKt.toByteArray(buffer.data))
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

extension WebRtcManager: RTCAudioSessionDelegate {
    func audioSession(_ audioSession: RTCAudioSession, didSetActive active: Bool) {
        if active {
            setSpeakerEnabled(enabled: deviceState.audioSpeakerEnabled)
        }
    }
    
    func audioSessionDidChangeRoute(_ session: RTCAudioSession, reason: AVAudioSession.RouteChangeReason, previousRoute: AVAudioSessionRouteDescription) {
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

extension RTCDataChannelState {
    func toDataChannelState() -> WebRtcManagerIListenerDataChannelState {
        switch self {
        case .connecting:
            return .connecting
        case .open:
            return .open
        case .closing:
            return .closing
        case .closed:
            return .closed
        @unknown default:
            return .closed
        }
    }
}

extension CMVideoDimensions {
    fileprivate func toSize() -> Size {
        Size(width: width, height: height)
    }
}
