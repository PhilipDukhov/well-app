package com.well.sharedMobile.puerh.call.imageSharing

import com.well.serverModels.Color
import com.well.serverModels.Path
import com.well.serverModels.Point
import com.well.serverModels.Size
import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.toSetOf
import com.well.utils.withEmptySet

object ImageSharingFeature {
    fun initialState(role: State.Role) =
        State(role) to if (role == State.Role.Editor) {
            setOf(Eff.SendInit, Eff.RequestImageUpdate)
        } else {
            setOf()
        }

    data class State(
        val role: Role,
        val localViewSize: Size? = null,
        val remoteViewSize: Size? = null,
        val image: ImageContainer? = null,
        val currentColor: Color = Color.drawingColors.first(),
        val lineWidth: Float = 2F,
        val paths: List<Path> = listOf(),
        val pathsHistory: List<List<Path>> = listOf(listOf()),
        val pathHistoryIndex: Int = 0,
        val undoAvailable: Boolean = false,
        val redoAvailable: Boolean = false,
    ) {
        companion object {
            val drawingColors = Color.drawingColors
            val lineWidthRange = 1.0..10.0
        }

        enum class Role {
            Editor,
            Viewer,
        }
    }

    sealed class Msg {
        object SwitchToViewer : Msg()
        data class UpdateLocalViewSize(val size: Size) : Msg()
        data class UpdateRemoteViewSize(val size: Size) : Msg()
        object ImageUpdateCancelled : Msg()
        data class LocalUpdateImage(val image: ImageContainer) : Msg()
        data class RemoteUpdateImage(val image: ImageContainer) : Msg()
        data class UpdateColor(val color: Color) : Msg()
        data class UpdateLineWidth(val lineWidth: Float) : Msg()
        object StartPath : Msg()
        data class AddPoint(val point: Point) : Msg()
        object EndPath : Msg()
        data class Erase(val point: Point) : Msg()
        data class UpdatePaths(val paths: List<Path>) : Msg()
        object Undo : Msg()
        object Redo : Msg()
        object Close : Msg()
    }

    sealed class Eff {
        object SendInit : Eff()
        object RequestImageUpdate : Eff()
        object Close : Eff()
        data class NotifyViewSizeUpdate(val size: Size) : Eff()
        data class UploadImage(
            val image: ImageContainer,
            val remoteViewSize: Size
        ) : Eff()

        data class UploadPaths(
            val paths: List<Path>
        ) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                Msg.Close -> {
                    return@eff Eff.Close
                }
                Msg.SwitchToViewer -> {
                    return@state state.copy(role = State.Role.Viewer)
                }
                is Msg.UpdateLocalViewSize -> {
                    return@reducer state.reduceLocalViewSizeUpdate(msg.size)
                }
                is Msg.UpdateRemoteViewSize -> {
                    return@state state.copy(remoteViewSize = msg.size)
                }
                Msg.ImageUpdateCancelled -> {
                    return@reducer state toSetOf if (state.image != null) {
                        null
                    } else {
                        Eff.Close
                    }
                }
                is Msg.LocalUpdateImage -> {
                    return@reducer state.copy(image = msg.image)
                        .reduceSendImageUpdate()
                }
                is Msg.RemoteUpdateImage -> {
                    return@state state.copy(image = msg.image)
                }
                is Msg.UpdateColor -> {
                    return@state state.copy(currentColor = msg.color)
                }
                is Msg.UpdateLineWidth -> {
                    return@state state.copy(lineWidth = msg.lineWidth)
                }
                Msg.StartPath -> {
                    return@state state.copy(
                        paths = state.paths + Path(
                            listOf(),
                            state.currentColor,
                            state.lineWidth
                        )
                    )
                }
                is Msg.AddPoint -> {
                    return@reducer state.copyAddPoint(msg.point).reduceSendPathsUpdate()
                }
                Msg.EndPath -> {
                    return@state state.copyAddPathsToHistory()
                }
                is Msg.Erase -> {
                    return@reducer state.reduceErasePoint(msg.point)
                }
                is Msg.UpdatePaths -> {
                    return@state state.copy(paths = msg.paths)
                }
                Msg.Undo -> {
                    if (!state.undoAvailable) throw IllegalStateException("undo unavailable")
                    return@state state.copyPathsHistoryIndexDiff(diff = -1)
                }
                Msg.Redo -> {
                    if (!state.redoAvailable) throw IllegalStateException("redo unavailable")
                    return@state state.copyPathsHistoryIndexDiff(diff = +1)
                }
            }
        })
    }.withEmptySet()

    private fun State.reduceLocalViewSizeUpdate(size: Size) =
        if (localViewSize != size) {
            copy(localViewSize = size) toSetOf Eff.NotifyViewSizeUpdate(size)
        } else {
            this.withEmptySet()
        }

    private fun State.reduceSendImageUpdate() =
        this toSetOf if (role == State.Role.Editor && image != null && remoteViewSize != null) {
            Eff.UploadImage(image, remoteViewSize)
        } else {
            null
        }

    private fun State.reduceSendPathsUpdate() =
        this toSetOf if (role == State.Role.Editor) {
            Eff.UploadPaths(paths)
        } else {
            null
        }

    private fun State.copyAddPathsToHistory(): State =
        copy(
            pathsHistory = pathsHistory + listOf(paths),
        ).copyPathsHistoryIndexDiff(diff = +1)

    private fun State.copyPathsHistoryIndexDiff(diff: Int): State =
        (pathHistoryIndex + diff).let { newIndex ->
            copy(
                pathHistoryIndex = newIndex,
                undoAvailable = role == State.Role.Editor && pathsHistory.isNotEmpty() && newIndex > 0,
                redoAvailable = role == State.Role.Editor && pathsHistory.isNotEmpty() && newIndex < pathsHistory.lastIndex
            )
        }

    private fun State.copyAddPoint(point: Point): State {
        val currentPath = paths.last()
        val paths = paths.toMutableList()
//        if (currentPath.points.count() > 100) {
//            paths.add(
//                currentPath.copy(
//                    points = listOf(
//                        currentPath.points.last(),
//                        msg.point
//                    )
//                )
//            )
//        } else {
        paths[paths.count() - 1] =
            currentPath.copy(points = currentPath.points + point)
//        }
        return copy(paths = paths)
    }

    private fun State.reduceErasePoint(point: Point): Pair<State, Set<Eff>> {
        val filtered = paths.filter { path ->
            path.points.any {
                it.intersects(point, path.lineWidth + 4)
            }
        }
        return if (filtered.count() != paths.count()) {
            copy(paths = filtered).reduceSendPathsUpdate()
        } else {
            this.withEmptySet()
        }
    }
}