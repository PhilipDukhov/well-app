package com.well.sharedMobile.puerh.call

import com.well.serverModels.*
import com.well.sharedMobile.puerh.call.CallFeature.State.Status.*
import com.well.utils.nativeFormat
import com.well.utils.toSetOf
import com.well.utils.withEmptySet
import com.well.utils.nextEnumValue

private val testDate = Date()

object CallFeature {
    fun callInitiateStateAndEffects(user: User) =
        State(user = user, status = Calling) toSetOf Eff.Initiate(user.id)

    fun incomingInitialState(incomingCall: WebSocketMessage.IncomingCall) =
        State(incomingCall, incomingCall.user, Incoming)

    fun testState(status: State.Status) =
        callInitiateStateAndEffects(
            User(
                1,
                "12",
                "12",
                User.Type.Test,
                "https://i.imgur.com/StXm8nf.jpg"
            )
        ).first.run {
            copy(
                status = status,
                callStartedDateInfo = State.CallStartedDateInfo(testDate),
                deviceState = deviceState.copy(cameraEnabled = false)
            )
        }

    data class State(
        val incomingCall: WebSocketMessage.IncomingCall? = null,
        val user: User,
        val status: Status,
        val deviceState: DeviceState = DeviceState(),
        val callStartedDateInfo: CallStartedDateInfo? = null,
        val localVideoContext: VideoViewContext? = null,
        val remoteVideoContext: VideoViewContext? = null,
    ) {
        data class CallStartedDateInfo(val date: Date) {
            val secondsPassedFormatted: String
                get() = date.secondsSinceNow().toInt().let {
                    String.nativeFormat("%02d:%02d", it / 60, it % 60)
                }
        }

        data class DeviceState(
            val micEnabled: Boolean = true,
            val cameraEnabled: Boolean = true,
            val audioSpeakerEnabled: Boolean = false,
            val isFrontCamera: Boolean = true,
        ) {
            fun toggleMicMsg(): Msg =
                Msg.SetMicEnabled(!micEnabled)

            fun toggleCameraMsg(): Msg =
                Msg.SetCameraEnabled(!cameraEnabled)

            fun toggleAudioSpeakerMsg(): Msg =
                Msg.SetAudioSpeakerEnabled(!audioSpeakerEnabled)

            fun toggleIsFrontCameraMsg(): Msg =
                Msg.SetIsFrontCamera(!isFrontCamera)
        }

        enum class Status {
            Calling,
            Incoming,
            Connecting,
            Ongoing,
            ;

            val stringRepresentation: String
                get() = when (this) {
                    Calling -> "Calling..."
                    Incoming -> "Incoming"
                    Connecting -> "Connecting..."
                    Ongoing -> "Ongoing"
                }
        }

        fun reduceCopyDeviceState(
            modifier: DeviceState.() -> DeviceState
        ) = modifier(deviceState).let {
            copy(deviceState = it) toSetOf Eff.UpdateDeviceState(it)
        }

        fun testIncStatus() = copy(status = status.nextEnumValue())
    }

    sealed class Msg {
        object Accept : Msg()
        object End : Msg()
        object DataConnectionEstablished : Msg()
        data class UpdateStatus(val status: State.Status) : Msg()
        data class UpdateLocalVideoContext(val viewContext: VideoViewContext) : Msg()
        data class UpdateRemoteVideoContext(val viewContext: VideoViewContext) : Msg()
        data class SetMicEnabled(val enabled: Boolean) : Msg()
        data class SetCameraEnabled(val enabled: Boolean) : Msg()
        data class SetAudioSpeakerEnabled(val enabled: Boolean) : Msg()
        data class SetIsFrontCamera(val isFrontCamera: Boolean) : Msg()
        object StartImageSharing : Msg()
    }

    sealed class Eff {
        data class Initiate(val userId: UserId) : Eff()
        data class Accept(val incomingCall: WebSocketMessage.IncomingCall) : Eff()
        object End : Eff()
        object StartImageSharing : Eff()
        data class UpdateDeviceState(val deviceState: State.DeviceState) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Accept -> state.incomingCall?.let { incomingCall ->
            state.copy(status = Connecting) to setOf(Eff.Accept(incomingCall))
        } ?: throw IllegalStateException("$msg | $state")
        is Msg.End -> state toSetOf Eff.End
        is Msg.UpdateLocalVideoContext -> {
            state.copy(localVideoContext = msg.viewContext).withEmptySet()
        }
        is Msg.UpdateRemoteVideoContext -> {
            state.copy(remoteVideoContext = msg.viewContext).withEmptySet()
        }
        is Msg.UpdateStatus -> {
            state.copy(status = msg.status).withEmptySet()
        }
        is Msg.SetMicEnabled -> {
            state.reduceCopyDeviceState { copy(micEnabled = msg.enabled) }
        }
        is Msg.SetCameraEnabled -> {
            state.reduceCopyDeviceState { copy(cameraEnabled = msg.enabled) }
        }
        is Msg.SetAudioSpeakerEnabled -> {
            state.reduceCopyDeviceState { copy(audioSpeakerEnabled = msg.enabled) }
        }
        is Msg.SetIsFrontCamera -> {
            state.reduceCopyDeviceState { copy(isFrontCamera = msg.isFrontCamera) }
        }
        is Msg.StartImageSharing -> {
            state toSetOf Eff.StartImageSharing
        }
        is Msg.DataConnectionEstablished -> {
            state.copy(
                status = Ongoing,
                callStartedDateInfo = state.callStartedDateInfo ?: State.CallStartedDateInfo(Date())
            ).withEmptySet()
        }
    }
}