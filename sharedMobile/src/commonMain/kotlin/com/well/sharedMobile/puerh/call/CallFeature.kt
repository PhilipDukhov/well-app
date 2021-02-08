package com.well.sharedMobile.puerh.call

import com.well.serverModels.*
import com.well.sharedMobile.puerh.call.CallFeature.State.Status.*
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.State as DrawingState
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature
import com.well.sharedMobile.puerh.call.webRtc.LocalDeviceState
import com.well.sharedMobile.puerh.call.webRtc.RemoteDeviceState
import com.well.utils.*

object CallFeature {
    fun callingStateAndEffects(user: User) =
        (State(user = user, status = Calling) toSetOf Eff.Initiate(user.id))
            .reduceInitialState()

    fun incomingStateAndEffects(incomingCall: WebSocketMessage.IncomingCall) =
        State(incomingCall, incomingCall.user, Incoming)
            .withEmptySet<State, Eff>()
            .reduceInitialState()

    private fun Pair<State, Set<Eff>>.reduceInitialState(): Pair<State, Set<Eff>> =
        first to (second + Eff.SyncLocalDeviceState(first.localDeviceState))

    private val testDate = Date()
    fun testState(status: State.Status) =
        callingStateAndEffects(
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
                localDeviceState = localDeviceState.copy(cameraEnabled = false),
                localCaptureDimensions = Size(1080, 1920),
                remoteCaptureDimensions = Size(1080, 1920),
            )
        }

    data class State(
        val incomingCall: WebSocketMessage.IncomingCall? = null,
        val user: User,
        val status: Status,
        val localDeviceState: LocalDeviceState = LocalDeviceState.default,
        val remoteDeviceState: RemoteDeviceState? = null,
        val callStartedDateInfo: CallStartedDateInfo? = null,
        val viewPoint: ViewPoint = ViewPoint.Both,
        val controlSet: ControlSet = ControlSet.Call,
        internal val localVideoContext: VideoViewContext? = null,
        internal val localCaptureDimensions: Size? = null,
        internal val remoteVideoContext: VideoViewContext? = null,
        internal val remoteCaptureDimensions: Size? = null,
        val drawingState: DrawingState = DrawingState(),
    ) {
        internal val localCameraEnabled = localDeviceState.cameraEnabled
        internal val remoteCameraEnabled = remoteDeviceState?.cameraEnabled != false
        val localVideoView = if (localVideoContext == null) null else VideoView(
            context = localVideoContext,
            hidden = !localCameraEnabled || viewPoint == ViewPoint.Partner,
            position = when (viewPoint) {
                ViewPoint.Partner,
                ViewPoint.Both,
                -> VideoView.Position.Minimized
                ViewPoint.Mine -> VideoView.Position.FullScreen
            }
        )
        val remoteVideoView = if (remoteVideoContext == null) null else VideoView(
            context = remoteVideoContext,
            hidden = !remoteCameraEnabled || viewPoint == ViewPoint.Mine,
            position = VideoView.Position.FullScreen
        )

        data class CallStartedDateInfo(val date: Date) {
            val secondsPassedFormatted: String
                get() = date.secondsSinceNow().toInt().let {
                    String.nativeFormat("%02d:%02d", it / 60, it % 60)
                }
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

        data class VideoView(
            val context: VideoViewContext,
            val hidden: Boolean,
            val position: Position,
        ) {
            enum class Position {
                FullScreen,
                Minimized,
                ;
            }
        }

        enum class ViewPoint {
            Both,
            Mine,
            Partner,
            ;
        }

        enum class ControlSet {
            Call,
            Drawing,
            ;
        }

//        fun testIncStatus() = copy(status = status.nextEnumValue())
    }

    sealed class Msg {
        object Accept : Msg()
        object End : Msg()
        object Back : Msg()
        object DataConnectionEstablished : Msg()
        data class UpdateLocalCaptureDimensions(val dimensions: Size) : Msg()
        data class UpdateRemoteCaptureDimensions(val dimensions: Size) : Msg()
        data class UpdateStatus(val status: State.Status) : Msg()
        data class UpdateLocalVideoContext(val viewContext: VideoViewContext) : Msg()
        data class UpdateRemoteVideoContext(val viewContext: VideoViewContext) : Msg()
        data class UpdateRemoteDeviceState(val deviceState: RemoteDeviceState) : Msg()
        data class SetMicEnabled(val enabled: Boolean) : Msg()
        data class SetCameraEnabled(val enabled: Boolean) : Msg()
        data class SetAudioSpeakerEnabled(val enabled: Boolean) : Msg()
        data class SetIsFrontCamera(val isFrontCamera: Boolean) : Msg()
        object InitializeDrawing : Msg()
        data class LocalUpdateViewPoint(val viewPoint: State.ViewPoint) : Msg()
        data class RemoteUpdateViewPoint(val viewPoint: State.ViewPoint) : Msg()
        data class UpdateControlSet(val controlSet: State.ControlSet) : Msg()
        data class DrawingMsg(val msg: DrawingFeature.Msg) : Msg()
    }

    sealed class Eff {
        data class Initiate(val userId: UserId) : Eff()
        data class Accept(val incomingCall: WebSocketMessage.IncomingCall) : Eff()
        object End : Eff()
        object ChooseViewPoint : Eff()
        object SystemBack : Eff()
        data class SyncLocalDeviceState(val localDeviceState: LocalDeviceState) : Eff()
        data class NotifyLocalCaptureDimensionsChanged(val dimensions: Size) : Eff()
        data class NotifyDeviceStateChanged(val deviceState: RemoteDeviceState) : Eff()
        data class NotifyUpdateViewPoint(val viewPoint: State.ViewPoint) : Eff()
        data class DrawingEff(val eff: DrawingFeature.Eff) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Accept -> state.incomingCall?.let { incomingCall ->
            state.copy(status = Connecting) to setOf(Eff.Accept(incomingCall))
        } ?: throw IllegalStateException("$msg | $state")
        is Msg.End -> state toSetOf Eff.End
        is Msg.Back -> {
            when (state.controlSet) {
                State.ControlSet.Call -> {
                    state toSetOf Eff.SystemBack
                }
                State.ControlSet.Drawing -> {
                    state.reduceUpdateControlSet(State.ControlSet.Call)
                }
            }
        }
        is Msg.UpdateLocalCaptureDimensions -> {
            println("UpdateLocalCaptureDimensions NotifyLocalCaptureDimensionsChanged ${state.status}")
            state.copy(localCaptureDimensions = msg.dimensions) toSetOf
                Eff.NotifyLocalCaptureDimensionsChanged(msg.dimensions)
        }
        is Msg.UpdateRemoteCaptureDimensions -> {
            state.copy(remoteCaptureDimensions = msg.dimensions).withEmptySet()
        }
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
        is Msg.InitializeDrawing -> {
            state.reduceInitializeDrawing()
        }
        is Msg.LocalUpdateViewPoint -> {
            state.reduceUpdateViewPoint(msg.viewPoint)
        }
        is Msg.RemoteUpdateViewPoint -> {
            state.reduceRemoteUpdateViewPoint(msg.viewPoint)
        }
        is Msg.UpdateControlSet -> {
            state.reduceUpdateControlSet(msg.controlSet)
        }
        is Msg.UpdateRemoteDeviceState -> {
            state.copy(remoteDeviceState = msg.deviceState).withEmptySet()
        }
        is Msg.DataConnectionEstablished -> {
            state.copy(
                status = Ongoing,
                callStartedDateInfo = state.callStartedDateInfo ?: State.CallStartedDateInfo(Date())
            ) toSetOf state.localCaptureDimensions?.let {
                Eff.NotifyLocalCaptureDimensionsChanged(it)
            }
        }
        is Msg.DrawingMsg -> {
            state.reduceDrawingMsg(msg.msg)
        }
    }

    private fun State.reduceInitializeDrawing() = when {
        viewPoint != State.ViewPoint.Both -> {
            copy(
                controlSet = State.ControlSet.Drawing
            ).withEmptySet()
        }
        localCameraEnabled && remoteCameraEnabled -> {
            this toSetOf Eff.ChooseViewPoint
        }
        localCameraEnabled -> {
            this.reduceUpdateViewPoint(State.ViewPoint.Mine)
        }
        remoteCameraEnabled -> {
            this.reduceUpdateViewPoint(State.ViewPoint.Partner)
        }
        else -> {
            throw IllegalStateException("reduceInitializeDrawing $this")
        }
    }

    private fun State.reduceRemoteUpdateViewPoint(viewPoint: State.ViewPoint) =
        copy(
            viewPoint = viewPoint,
            controlSet = if (viewPoint == State.ViewPoint.Both)
                State.ControlSet.Call
            else
                controlSet,
            drawingState = drawingStateCopyViewPoint(viewPoint),
        ).reduceUpdateLocalDeviceState()

    private fun State.reduceUpdateViewPoint(viewPoint: State.ViewPoint) =
        copy(
            viewPoint = viewPoint,
            controlSet = if (viewPoint == State.ViewPoint.Both)
                State.ControlSet.Call
            else
                State.ControlSet.Drawing,
            drawingState = drawingStateCopyViewPoint(viewPoint),
        ).reduceUpdateLocalDeviceState() plus Eff.NotifyUpdateViewPoint(viewPoint)

    private fun State.drawingStateCopyViewPoint(viewPoint: State.ViewPoint) =
        drawingState.copy(
            videoViewSize = when (viewPoint) {
                State.ViewPoint.Both -> null
                State.ViewPoint.Mine -> localCaptureDimensions
                State.ViewPoint.Partner -> remoteCaptureDimensions
            }
        )

    private fun State.reduceDrawingMsg(msg: DrawingFeature.Msg): Pair<State, Set<Eff>> {
        val (state, effs) = DrawingFeature.reducer(msg, drawingState)
        return copy(
            drawingState = state,
        ) to effs.mapTo(HashSet(), Eff::DrawingEff)
    }

    private fun State.reduceUpdateControlSet(controlSet: State.ControlSet): Pair<State, Set<Eff>> =
        copy(
            controlSet = controlSet
        ).withEmptySet()

    private fun State.reduceCopyDeviceState(
        modifier: LocalDeviceState.() -> LocalDeviceState
    ) = reduceUpdateLocalDeviceState(modifier(localDeviceState))

    private fun State.reduceUpdateLocalDeviceState(
        state: LocalDeviceState = localDeviceState
    ) = copy(localDeviceState = state) to setOf(
            Eff.SyncLocalDeviceState(
                when (viewPoint) {
                    State.ViewPoint.Both,
                    State.ViewPoint.Mine -> state
                    State.ViewPoint.Partner -> state.copy(cameraEnabled = false)
                }
            ),
            if (state != localDeviceState) Eff.NotifyDeviceStateChanged(RemoteDeviceState(state)) else null,
        ).filterNotNull().toSet()
}

