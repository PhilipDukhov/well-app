package com.well.modules.androidWebrtc

import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainerImpl
import com.well.modules.features.call.callFeature.VideoViewContext
import com.well.modules.features.call.callFeature.webRtc.LocalDeviceState
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI
import com.well.modules.features.call.callFeature.webRtc.WebRtcManagerI.Listener.DataChannelState
import com.well.modules.models.Size
import com.well.modules.models.WebSocketMsg
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoCapturer
import org.webrtc.VideoTrack
import java.nio.ByteBuffer

class WebRtcManager(
    iceServers: List<String>,
    private val applicationContext: Context,
    private val listener: WebRtcManagerI.Listener,
) : CloseableContainerImpl(), WebRtcManagerI {

    companion object {
        private var initialized = false
    }

    private var deviceState = LocalDeviceState.default()
    private val audioManager = applicationContext.getSystemService(AudioManager::class.java)!!
    private val coroutineContext = CoroutineScope(Dispatchers.Default)

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
        addCloseableChild(object : Closeable {
            override fun close() {
                Napier.d("close")
                try {
                    peerConnection.dispose()
                    videoCapturer.dispose()
                    listOf(
                        localAudioTrack,
                        localVideoTrack,
                        remoteAudioTrack,
                        remoteVideoTrack,
                    ).forEach {
                        it?.dispose()
                    }
                } catch (e: Exception) {
                    Napier.d("close failed $e", e)
                }
            }
        })

        coroutineContext.launch {
            try {
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            } catch (e: Exception) {
                Napier.w("audioManager.mode $e")
            }
        }
        val listener = object : AudioManager.OnAudioFocusChangeListener, Closeable {
            private var hasAudioFocus = false
            lateinit var focusRequest: AudioFocusRequest

            override fun onAudioFocusChange(focusChange: Int) {
                hasAudioFocus = focusChange == AudioManager.AUDIOFOCUS_GAIN
            }

            override fun close() {
                if (hasAudioFocus) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.abandonAudioFocusRequest(focusRequest)
                    } else {
                        @Suppress("DEPRECATION")
                        audioManager.abandonAudioFocus(this)
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            listener.focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                .setOnAudioFocusChangeListener(listener)
                .build()
            audioManager.requestAudioFocus(
                listener.focusRequest
            )
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                listener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN,
            )
        }
        addCloseableChild(listener)
        audioManager.isSpeakerphoneOn = deviceState.audioSpeakerEnabled
    }

    private val rootEglBase = EglBase.create()!!
    private val enumerator = if (Camera2Enumerator.isSupported(applicationContext)) {
        Camera2Enumerator(applicationContext)
    } else {
        Camera1Enumerator(true)
    }
    private val videoCapturer = createVideoCapturer(deviceState.isFrontCamera)!!
    private val factory = PeerConnectionFactory.builder()
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
        with(videoCapturer) {
            val videoSource = factory.createVideoSource(isScreencast)
            initialize(
                SurfaceTextureHelper.create("WebRTC", rootEglBase.eglBaseContext),
                applicationContext,
                videoSource.capturerObserver
            )
            startCapture()
            videoSource
        }
    )
    override val localVideoContext: VideoViewContext
        get() = VideoViewContext(
            rootEglBase,
            localVideoTrack
        )
    private val localAudioTrack = factory.createAudioTrack(
        "101",
        factory.createAudioSource(MediaConstraints())
    )!!.apply {
        setEnabled(deviceState.micEnabled)
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
            ).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            },
            object : PeerConnectionObserver() {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    CoroutineScope(Dispatchers.IO).launch {
                        listener.addCandidate(
                            WebSocketMsg.Call.Candidate(
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
                        object : DataChannelObserver(dataChannel, "remote") {
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
        registerObserver(object : DataChannelObserver(this, "local") {
            override fun onStateChange(state: DataChannel.State) {
                super.onStateChange(state)
                listener.dataChannelStateChanged(state.toDataChannelState())
            }
        })
    }

    override val manyCamerasAvailable: Boolean
        get() = enumerator.deviceNames.map { enumerator.isFrontFacing(it) }.toSet().count() == 2

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

    override fun acceptCandidate(candidate: WebSocketMsg.Call.Candidate) {
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
            Napier.e("didn't sendData state ${localDataChannel.state()}")
        }
    }

    override fun syncDeviceState(deviceState: LocalDeviceState) {
        if (this.deviceState.isFrontCamera != deviceState.isFrontCamera) {
            switchCamera()
        }
        if (this.deviceState.micEnabled != deviceState.micEnabled) {
            localAudioTrack.setEnabled(deviceState.micEnabled)
        }
        if (this.deviceState.audioSpeakerEnabled != deviceState.audioSpeakerEnabled) {
            audioManager.isSpeakerphoneOn = deviceState.audioSpeakerEnabled
        }
        if (this.deviceState.cameraEnabled != deviceState.cameraEnabled) {
            if (deviceState.cameraEnabled) {
                videoCapturer.startCapture()
            } else {
                videoCapturer.stopCapture()
            }
            localVideoTrack.setEnabled(deviceState.cameraEnabled)
        }
        this.deviceState = deviceState
    }

    private fun switchCamera() {
        videoCapturer.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
            override fun onCameraSwitchDone(p0: Boolean) {
                if (p0 != this@WebRtcManager.deviceState.isFrontCamera) {
                    this@WebRtcManager.switchCamera()
                }
            }

            override fun onCameraSwitchError(p0: String?) {
            }
        })
    }

    private fun VideoCapturer.startCapture() {
        val cameraConfig = if (isProbablyAnEmulator())
            Triple(320, 240, 15)
        else
            Triple(1280, 720, 30)
        startCapture(cameraConfig.first, cameraConfig.second, cameraConfig.third)
        coroutineContext.launch {
            delay(100)
            listener.updateCaptureDimensions(Size(cameraConfig.second, cameraConfig.first))
        }
    }

    private fun createOfferOrAnswer(
        create: PeerConnection.(SdpObserver, MediaConstraints) -> Unit,
        completion: (String) -> Unit,
    ) {
        peerConnection.addTrack(localVideoTrack, listOf("track"))
        peerConnection.addTrack(localAudioTrack, listOf("track"))
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

    private fun createVideoCapturer(frontFacing: Boolean): CameraVideoCapturer? =
        enumerator.deviceNames
            .sortedBy { frontFacing != enumerator.isFrontFacing(it) }
            .firstNotNullOfOrNull {
                enumerator.createCapturer(it, null)
            }

    private fun DataChannel.State.toDataChannelState(): DataChannelState =
        when (this) {
            DataChannel.State.CONNECTING -> DataChannelState.Connecting
            DataChannel.State.OPEN -> DataChannelState.Open
            DataChannel.State.CLOSING -> DataChannelState.Closing
            DataChannel.State.CLOSED -> DataChannelState.Closed
        }


    private fun isProbablyAnEmulator(): Boolean {
        return  (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}