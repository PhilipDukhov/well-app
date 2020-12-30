package com.well.androidApp.ui.webRtc

import android.content.Context
import com.well.androidApp.utils.Utilities
import com.well.androidApp.utils.firstMapOrNull
import com.well.serverModels.WebSocketMessage
import com.well.shared.puerh.call.SurfaceViewContext
import com.well.shared.puerh.call.WebRtcManagerI
import com.well.utils.CloseableContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.webrtc.*

class WebRtcManager(
    private val applicationContext: Context,
    private val listener: WebRtcManagerI.Listener,
): CloseableContainer(), WebRtcManagerI {
    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory
                .InitializationOptions
                .builder(applicationContext)
                .createInitializationOptions()
        )
    }
    private val rootEglBase = EglBase.create()!!
    private val factory = PeerConnectionFactory
        .builder()
        .setOptions(PeerConnectionFactory.Options())
        .setVideoEncoderFactory(DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true,
            true
        ))
        .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
        .createPeerConnectionFactory()!!
    private val localVideoTrack = factory.createVideoTrack(
        "ARDAMSv0",
        createVideoCapturer()!!.run {
            val videoSource = factory.createVideoSource(isScreencast)
            initialize(
                SurfaceTextureHelper.create("WebRTC", rootEglBase.eglBaseContext),
                applicationContext,
                videoSource.capturerObserver
            )
            if (Utilities.isProbablyAnEmulator())
                startCapture(320, 240, 15)
            else
                startCapture(1280, 720, 30)
            videoSource
        }
    )
    override val localVideoContext = SurfaceViewContext(
        rootEglBase,
        localVideoTrack
    )
    private val localAudioTrack = factory.createAudioTrack(
        "101",
        factory.createAudioSource(MediaConstraints())
    )?.apply {
        this.setVolume(0.0)
    }
    private val localMediaStream = factory.createLocalMediaStream("ARDAMS")
        .apply {
            addTrack(localVideoTrack)
            addTrack(localAudioTrack)
        }
    private var remoteVideoTrack: VideoTrack? = null
        set(value) {
            field?.dispose()
            field = value
            field?.setEnabled(true)
        }
    private var remoteAudioTrack: AudioTrack? = null
        set(value) {
            field?.dispose()
            field = value
            field?.setEnabled(true)
        }
    private val peerConnection by lazy {
        factory.createPeerConnection(
            PeerConnection.RTCConfiguration(
                listOf(
                    "stun:stun.l.google.com:19302",
                ).map {
                    PeerConnection
                        .IceServer
                        .builder(it)
                        .createIceServer()
                }
            ),
            object : PeerConnectionObserver() {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    CoroutineScope(Dispatchers.IO).launch {
                        candidates.emit(
                            WebSocketMessage.Candidate(
                                iceCandidate.sdpMid,
                                iceCandidate.sdpMLineIndex,
                                iceCandidate.sdp,
                            )
                        )
                    }
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    remoteVideoTrack = mediaStream.videoTracks.firstOrNull()
                    remoteAudioTrack = mediaStream.audioTracks.firstOrNull()
                    listener.updateRemoveVideoContext(
                        remoteVideoTrack?.let {
                            SurfaceViewContext(
                                rootEglBase,
                                it
                            )
                        }
                    )
                }
            })!!
    }
    override var candidates = MutableSharedFlow<WebSocketMessage.Candidate>(replay = Int.MAX_VALUE)

    private fun createVideoCapturer(): VideoCapturer? =
        if (Camera2Enumerator.isSupported(applicationContext)) {
            Camera2Enumerator(applicationContext)
        } else {
            Camera1Enumerator(true)
        }.let { enumerator ->
            enumerator.deviceNames
                .sortedBy { !enumerator.isFrontFacing(it) }  // first try to find front camera
                .firstMapOrNull {
                    enumerator.createCapturer(it, null)
                }
        }

    override fun sendOffer() {
        peerConnection.addStream(localMediaStream)
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        peerConnection.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                listener.sendOffer(sessionDescription.description)
            }
        }, sdpMediaConstraints)
    }

    override fun acceptOffer(webRTCSessionDescriptor: String) {
        peerConnection.addStream(localMediaStream)
        peerConnection.setRemoteDescription(
            SimpleSdpObserver(),
            SessionDescription(
                SessionDescription.Type.OFFER,
                webRTCSessionDescriptor,
            )
        )
        peerConnection.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                listener.sendAnswer(sessionDescription.description)
            }
        }, MediaConstraints())
    }

    override fun acceptAnswer(webRTCSessionDescriptor: String) {
        peerConnection.setRemoteDescription(
            SimpleSdpObserver(),
            SessionDescription(
                SessionDescription.Type.ANSWER,
                webRTCSessionDescriptor,
            )
        )
    }

    override fun acceptCandidate(candidate: WebSocketMessage.Candidate) {
        peerConnection.addIceCandidate(
            IceCandidate(
                candidate.id,
                candidate.label,
                candidate.candidate
            )
        )
    }
}
