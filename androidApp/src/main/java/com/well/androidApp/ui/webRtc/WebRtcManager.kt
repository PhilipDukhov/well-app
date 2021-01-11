package com.well.androidApp.ui.webRtc

import android.content.Context
import com.well.androidApp.utils.Utilities
import com.well.androidApp.utils.firstMapOrNull
import com.well.serverModels.WebSocketMessage
import com.well.sharedMobile.puerh.call.VideoViewContext
import com.well.sharedMobile.puerh.call.WebRtcManagerI
import com.well.utils.CloseableContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.*
import java.nio.ByteBuffer

class WebRtcManager(
    iceServers: List<String>,
    private val applicationContext: Context,
    private val listener: WebRtcManagerI.Listener,
) : CloseableContainer(), WebRtcManagerI {

    companion object {
        private var initialized = false
    }

    init {
        if (!initialized) {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory
                    .InitializationOptions
                    .builder(applicationContext)
                    .createInitializationOptions()
            )
            initialized = true
        }
    }

    private val rootEglBase = EglBase.create()!!
    private val factory = PeerConnectionFactory
        .builder()
        .setOptions(PeerConnectionFactory.Options())
        .setVideoEncoderFactory(
            DefaultVideoEncoderFactory(
                rootEglBase.eglBaseContext,
                true,
                true
            )
        )
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
    override val localVideoContext = VideoViewContext(
        rootEglBase,
        localVideoTrack
    )
    private val localAudioTrack = factory.createAudioTrack(
        "101",
        factory.createAudioSource(MediaConstraints())
    )
        ?.apply {
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
            listener.updateRemoveVideoContext(
                field?.let {
                    VideoViewContext(
                        rootEglBase,
                        it
                    )
                }
            )
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
                iceServers.map {
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
                        listener.addCandidate(
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
                }

                override fun onRemoveStream(mediaStream: MediaStream) {
                    super.onRemoveStream(mediaStream)
                    remoteVideoTrack = null
                    remoteAudioTrack = null
                }

                override fun onDataChannel(dataChannel: DataChannel) {
                    super.onDataChannel(dataChannel)
                    dataChannel.registerObserver(
                        object : DataChannelObserver() {
                            override fun onMessage(p0: DataChannel.Buffer?) {
                                super.onMessage(p0)
                                p0?.data?.let {
                                    val byteArray = ByteArray(it.capacity())
                                    it.get(byteArray)
                                    listener.receiveData(byteArray)
                                }
                            }
                        }
                    )
                }
            })!!
    }
    private val localDataChannel = peerConnection.createDataChannel(
        "WebRTCData",
        DataChannel.Init()
    ).apply {
        registerObserver(
            object : DataChannelObserver() {
                override fun onStateChange() {
                    super.onStateChange()
                }
            }
        )
    }

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
        createOfferOrAnswer(
            PeerConnection::createOffer,
            listener::sendOffer,
        )
    }

    override fun acceptOffer(webRTCSessionDescriptor: String) {
        peerConnection.setRemoteDescription(
            SimpleSdpObserver(),
            SessionDescription(
                SessionDescription.Type.OFFER,
                webRTCSessionDescriptor,
            )
        )
        createOfferOrAnswer(
            PeerConnection::createAnswer,
            listener::sendAnswer,
        )
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
                candidate.sdpMid,
                candidate.sdpMLineIndex,
                candidate.sdp
            )
        )
    }

    override fun sendData(data: ByteArray) {
        if (localDataChannel.state() == DataChannel.State.OPEN) {
            localDataChannel.send(DataChannel.Buffer(ByteBuffer.wrap(data), false))
        } else {
            println("didn't sendData state ${localDataChannel.state()}")
        }
    }

    private fun createOfferOrAnswer(
        create: PeerConnection.(SdpObserver, MediaConstraints) -> Unit,
        completion: (String) -> Unit
    ) {
        peerConnection.addStream(localMediaStream)
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        peerConnection.create(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                completion(sessionDescription.description)
            }
        }, sdpMediaConstraints)
    }
}
