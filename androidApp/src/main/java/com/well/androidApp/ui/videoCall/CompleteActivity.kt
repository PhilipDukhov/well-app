package com.well.androidApp.ui.videoCall

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.well.androidApp.databinding.ActivitySamplePeerConnectionBinding
import com.well.androidApp.utils.firstMapOrNull
import com.well.serverModels.WebRTCMessage
import com.well.serverModels.WebRTCMessage.*
import com.well.shared.puerh.WebSocketManager
//import com.well.shared.puerh.call.CallFeature
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature
import com.well.utils.Closeable
import com.well.utils.EffectHandler
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.webrtc.*
import kotlin.coroutines.CoroutineContext

//class WebRTCManagerEffectHandler(
//    private val webSocketManager: WebSocketManager,
//    override val coroutineScope: CoroutineScope,
//) : EffectHandler<CallFeature.Eff, CallFeature.Msg>(coroutineScope) {
//
//    override fun handleEffect(eff: CallFeature.Eff) {
//
//    }
//}

class CompleteActivity : AppCompatActivity() {
    private var isInitiator = false
    private var isChannelReady = false
    private var isStarted = false
    lateinit var audioConstraints: MediaConstraints
    lateinit var audioSource: AudioSource
    lateinit var localAudioTrack: AudioTrack
    private lateinit var binding: ActivitySamplePeerConnectionBinding
    private lateinit var peerConnection: PeerConnection
    private lateinit var rootEglBase: EglBase
    private lateinit var factory: PeerConnectionFactory
    private lateinit var videoTrackFromCamera: VideoTrack
    private val client = HttpClient {
        install(WebSockets)
    }

    private var socketSession: DefaultClientWebSocketSession? = null

    private fun send(message: WebRTCMessage) {
        GlobalScope.launch {
            socketSession?.send(Json.encodeToString(message))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySamplePeerConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        start()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            println("wtfff $isGranted")
            if (isGranted) {
                start()
            }
        }

    private fun start() {
        val notGranted = permissions.firstOrNull {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) != PERMISSION_GRANTED
        }
        if (notGranted != null) {
            requestPermissionLauncher.launch(notGranted)
            return
        }
        GlobalScope.launch {
            connect()
        }
        initializeSurfaceViews()
        initializePeerConnectionFactory()
        createVideoTrackFromCameraAndShowIt()
        initializePeerConnections()
        startStreamingVideo()
    }

    private val permissions = listOf(CAMERA, RECORD_AUDIO)

    // MirtDPM4
    private fun doAnswer() {
        peerConnection.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                send(Answer(sessionDescription.description))
            }
        }, MediaConstraints())
    }

    private fun maybeStart() {
        println("maybeStart: $isStarted $isChannelReady")
        if (!isStarted && isChannelReady) {
            isStarted = true
            if (isInitiator) {
                doCall()
            }
        }
    }

    private fun doCall() {
        val sdpMediaConstraints = MediaConstraints()
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        )
        sdpMediaConstraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")
        )
        peerConnection.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                println("onCreateSuccess: ")
                peerConnection.setLocalDescription(SimpleSdpObserver(), sessionDescription)
                send(Offer(sessionDescription.description))
            }
        }, sdpMediaConstraints)
    }

    private fun initializeSurfaceViews() {
        rootEglBase = EglBase.create()
        binding.surfaceView.init(rootEglBase.eglBaseContext, null)
        binding.surfaceView.setEnableHardwareScaler(true)
        binding.surfaceView.setMirror(true)
        binding.surfaceView2.init(rootEglBase.eglBaseContext, null)
        binding.surfaceView2.setEnableHardwareScaler(true)
        binding.surfaceView2.setMirror(true)
    }

    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(this)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            rootEglBase.eglBaseContext,
            true,
            true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(rootEglBase.eglBaseContext)
        factory = PeerConnectionFactory
            .builder()
            .setOptions(PeerConnectionFactory.Options())
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()
    }

    private fun createVideoTrackFromCameraAndShowIt() {
        audioConstraints = MediaConstraints()
        val videoCapturer = createVideoCapturer()!!
        val videoSource = factory.createVideoSource(videoCapturer.isScreencast)
        videoCapturer.initialize(
            SurfaceTextureHelper.create("WebRTC", rootEglBase.eglBaseContext),
            this,
            videoSource.capturerObserver
        )
        videoCapturer.startCapture(1280, 720, 30)
        videoTrackFromCamera = factory.createVideoTrack("ARDAMSv0", videoSource)
        videoTrackFromCamera.setEnabled(true)
        videoTrackFromCamera.addSink(binding.surfaceView)

        // create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints)
        localAudioTrack = factory.createAudioTrack("101", audioSource)
    }

    private fun initializePeerConnections() {
        peerConnection = factory.createPeerConnection(
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
                    send(
                        Candidate(
                            iceCandidate.sdpMid,
                            iceCandidate.sdpMLineIndex,
                            iceCandidate.sdp
                        )
                    )
                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    val remoteVideoTrack = mediaStream.videoTracks[0]
                    val remoteAudioTrack = mediaStream.audioTracks[0]
                    remoteAudioTrack.setEnabled(true)
                    remoteVideoTrack.setEnabled(true)
                    remoteVideoTrack.addSink(binding.surfaceView2)
                }
            })!!
    }

    private fun startStreamingVideo() {
        val mediaStream = factory.createLocalMediaStream("ARDAMS")
        mediaStream.addTrack(videoTrackFromCamera)
        mediaStream.addTrack(localAudioTrack)
        peerConnection.addStream(mediaStream)
    }

    private fun createVideoCapturer(): VideoCapturer? =
        if (Camera2Enumerator.isSupported(this)) {
            Camera2Enumerator(this)
        } else {
            Camera1Enumerator(true)
        }.let { enumerator ->
            enumerator.deviceNames
                .sortedBy { !enumerator.isFrontFacing(it) }  // first try to find front camera
                .firstMapOrNull {
                    enumerator.createCapturer(it, null)
                }
        }

    private suspend fun connect() {
        client.ws(
            Get,
            "well-env.eba-bzjcehdy.us-east-2.elasticbeanstalk.com",
            8090,
            "/socket"
        )
        {
            socketSession = this
            send(Frame.Ping(byteArrayOf()))
            try {
                for (data in incoming) {
                    if (data !is Frame.Text) continue
                    when (val message: WebRTCMessage = Json.decodeFromString(data.readText())) {
                        is Created -> isInitiator = true
                        is Join, is Joined -> {
                            isChannelReady = true
                            maybeStart()
                        }
                        is Offer -> {
                            if (!isInitiator && !isStarted) {
                                maybeStart()
                            }
                            peerConnection.setRemoteDescription(
                                SimpleSdpObserver(),
                                SessionDescription(
                                    SessionDescription.Type.OFFER,
                                    message.sdp
                                )
                            )
                            doAnswer()
                        }
                        is Answer -> {
                            if (!isStarted) throw IllegalStateException()
                            peerConnection.setRemoteDescription(
                                SimpleSdpObserver(),
                                SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    message.sdp
                                )
                            )
                        }
                        is Candidate -> {
                            if (!isStarted) throw IllegalStateException()
                            val candidate = IceCandidate(
                                message.id,
                                message.label,
                                message.candidate
                            )
                            peerConnection.addIceCandidate(candidate)
                        }
                    }
                }
            } finally {
                socketSession = null
            }
        }
    }
}