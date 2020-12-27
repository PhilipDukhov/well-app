package com.well.androidApp.ui.videoCall

import android.content.Context
import com.well.androidApp.utils.Utilities
import com.well.androidApp.utils.firstMapOrNull
import com.well.serverModels.UserId
import com.well.serverModels.WebSocketMessage
import com.well.shared.puerh.WebSocketManager
import com.well.shared.puerh.call.CallFeature.State.Status
import com.well.shared.puerh.call.CallFeature.Eff
import com.well.shared.puerh.call.CallFeature.Msg
import com.well.shared.puerh.call.SurfaceViewContext
import com.well.utils.Closeable
import com.well.utils.EffectHandler
import com.well.utils.asCloseable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.*


class WebRTCManagerEffectHandler(
    private val webSocketManager: WebSocketManager,
    private val applicationContext: Context,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
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
                    coroutineScope.launch {
                        candidates.emit(iceCandidate)
                    }
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    listener?.invoke(Msg.UpdateStatus(Status.Ongoing))
                    remoteVideoTrack = mediaStream.videoTracks.firstOrNull()
                    remoteAudioTrack = mediaStream.audioTracks.firstOrNull()

                    remoteVideoTrack?.let { remoteVideoTrack ->
                        coroutineScope.launch {
                            listener?.invoke(
                                Msg.UpdateRemoteVideoContext(
                                    SurfaceViewContext(
                                        rootEglBase,
                                        remoteVideoTrack
                                    )
                                )
                            )
                        }
                    }
                }
            })!!
    }
    private var candidates = MutableSharedFlow<IceCandidate>(replay = Int.MAX_VALUE)
    private val candidatesSendState = webSocketManager
        .state
        .map { it == WebSocketManager.Status.Connected }
        .distinctUntilChanged()
    private var candidatesSendCloseable: Closeable? = null
        set(value) {
            field?.close()
            field = value
        }
    init {
        addCloseableChild(
            coroutineScope.launch {
                candidatesSendState
                    .collect { shouldSend ->
                        println("should send $shouldSend ${candidates.replayCache}")
                        candidatesSendCloseable =
                            if (shouldSend)
                                coroutineScope.launch {
                                    candidates.collect {
                                        println("listenWebSocketMessage send $it")
                                        webSocketManager.send(
                                            WebSocketMessage.Candidate(
                                                it.sdpMid,
                                                it.sdpMLineIndex,
                                                it.sdp,
                                            )
                                        )
                                    }
                                }.asCloseable()
                            else null
                    }
            }.asCloseable()
        )
        coroutineScope.launch {
            addCloseableChild(
                webSocketManager.addListener(::listenWebSocketMessage)
            )
        }
    }

    override fun setListener(listener: suspend (Msg) -> Unit) {
        super.setListener(listener)
        coroutineScope.launch {
            listener.invoke(
                Msg.UpdateLocalVideoContext(
                    SurfaceViewContext(
                        rootEglBase,
                        localVideoTrack
                    )
                )
            )
        }
    }

    override fun handleEffect(eff: Eff) {
        when (eff) {
            is Eff.Initiate ->
                initiateCall(eff.userId)
            is Eff.Accept -> {
                peerConnection.addStream(localMediaStream)
                sendOffer()
            }
            Eff.End -> close()
        }
    }

    fun send(message: WebSocketMessage) =
        coroutineScope.launch {
            webSocketManager.send(message)
        }

    private fun listenWebSocketMessage(msg: WebSocketMessage) {
        println("listenWebSocketMessage $msg")
        when (msg) {
            is WebSocketMessage.Offer -> {
                peerConnection.addStream(localMediaStream)
                listener?.invoke(Msg.UpdateStatus(Status.Connecting))
                acceptOffer(msg.sessionDescriptor)
            }
            is WebSocketMessage.Answer -> {
                added = true
                peerConnection.setRemoteDescription(
                    SimpleSdpObserver(),
                    SessionDescription(
                        SessionDescription.Type.ANSWER,
                        msg.sessionDescriptor
                    )
                )
            }
            is WebSocketMessage.Candidate -> {
                if (!added) {
                    added = true
                }
                println("onIceCandidate $msg")
                peerConnection.addIceCandidate(
                    IceCandidate(
                        msg.id,
                        msg.label,
                        msg.candidate
                    )
                )
            }
            else -> Unit
        }
    }

    var added = false

    private fun initiateCall(userId: UserId) =
        send(
            WebSocketMessage.InitiateCall(
                userId,
            )
        )

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

    private fun sendOffer() {
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
                send(
                    WebSocketMessage.Offer(
                        sessionDescription.description,
                    )
                )
            }
        }, sdpMediaConstraints)
    }

    private fun acceptOffer(webRTCSessionDescriptor: String) {
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
                send(WebSocketMessage.Answer(sessionDescription.description))
            }
        }, MediaConstraints())
    }
}