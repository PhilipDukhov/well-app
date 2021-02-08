package com.well.sharedMobile.puerh.call.drawing

import com.well.serverModels.*
import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.map
import com.well.utils.platform.Platform
import com.well.utils.platform.nativeScale
import com.well.utils.toSetOf
import com.well.utils.withEmptySet
import kotlin.time.Duration
import kotlin.time.measureTimedValue

object DrawingFeature {
    fun testState(imageContainer: ImageContainer?) =
        State()
            .copy(
                image = imageContainer,
            )

    data class State(
        val localViewSize: Size? = null,
        val remoteViewSize: Size? = null,
        val videoViewSize: Size? = null,
        val image: ImageContainer? = null,
        val currentColor: Color = Color.drawingColors.first(),
        val lineWidth: Float = 4F,
        val nativeStrokeStyle: StrokeStyle = StrokeStyle.default,
        val selectedBrush: Brush = Brush.Pen,
        internal val drawingPaths: List<Path> = emptyList(),
        internal val pathsHistory: List<List<Path>> = listOf(listOf()),
        internal val pathsHistoryIndex: Int = 0,
        internal val remotePaths: List<Path> = listOf(),
        val lastReduceDurations: List<Duration> = listOf(),
    ) {
        private val historyEditingAvailable = pathsHistory.isNotEmpty()
        val undoAvailable = historyEditingAvailable && pathsHistoryIndex != 0
        val redoAvailable = historyEditingAvailable && pathsHistoryIndex != pathsHistory.lastIndex
        internal val drawingConverter =
            localViewSize?.let { localViewSize ->
                image?.size?.let {
                    Converter(localViewSize, it, Converter.ContentMode.AspectFit)
                } ?: videoViewSize?.let {
                    Converter(localViewSize, it, Converter.ContentMode.AspectFill)
                }
            }
        internal val localPaths = pathsHistory[pathsHistoryIndex] + drawingPaths
        internal val currentPaths = (localPaths + remotePaths).sortedBy { it.date.millis }
        val canvasPaths = drawingConverter?.let { drawingConverter ->
            currentPaths.map { path ->
                path.copy(
                    points = path.points.map(drawingConverter::denormalize)
                )
            }
        } ?: listOf()

        companion object {
            val drawingColors = Color.drawingColors
            val lineWidthRange = 1F..50F
        }

        data class StrokeStyle(
            val lineCap: LineCap,
            val lineJoin: LineJoin
        ) {
            enum class LineCap {
                Butt,
                Round,
                Square,
            }

            enum class LineJoin {
                Miter,
                Round,
                Bevel,
            }

            companion object {
                val default = StrokeStyle(LineCap.Round, LineJoin.Round)
            }
        }

        sealed class Brush {
            object Pen : Brush()
            object Eraser : Brush()
        }

        internal class Converter(
            containerSize: Size,
            imageSize: Size,
            contentMode: ContentMode,
        ) {
            private val containerImageSize = when (contentMode) {
                ContentMode.AspectFit -> {
                    containerSize.aspectFit(imageSize)
                }
                ContentMode.AspectFill -> {
                    containerSize.aspectFill(imageSize)
                }
            }
            private val imageOffset = Point(
                x = (containerSize.width - containerImageSize.width) / 2,
                y = (containerSize.height - containerImageSize.height) / 2,
            )
            init {
                println("Converter init $containerImageSize $imageOffset")
            }

            fun normalize(point: Point) =
                Point(
                    x = (point.x - imageOffset.x) / containerImageSize.width,
                    y = (point.y - imageOffset.y) / containerImageSize.height
                )

            fun denormalize(point: Point) =
                Point(
                    x = point.x * containerImageSize.width + imageOffset.x,
                    y = point.y * containerImageSize.height + imageOffset.y,
                )

            enum class ContentMode {
                AspectFit,
                AspectFill,
                ;
            }
        }
    }

    sealed class Msg {
        data class UpdateLocalViewSize(val size: Size) : Msg()
        data class UpdateRemoteViewSize(val size: Size) : Msg()
//        object ImageUpdateCancelled : Msg()
        data class LocalUpdateImage(val image: ImageContainer) : Msg()
        data class RemoteUpdateImage(val image: ImageContainer) : Msg()
        data class UpdateColor(val color: Color) : Msg()
        data class UpdateLineWidth(val lineWidth: Float) : Msg()
        data class NewDragPoint(val point: Point) : Msg()
        data class SelectBrush(val brush: State.Brush): Msg()
        object EndDrag : Msg()
        data class UpdatePaths(val paths: List<Path>) : Msg()
        object Undo : Msg()
        object Redo : Msg()
        data class LocalClear(val saveHistory: Boolean) : Msg()
        data class RemoteClear(val saveHistory: Boolean, val date: Date) : Msg()
    }

    sealed class Eff {
//        object RequestImageUpdate : Eff()
        data class NotifyViewSizeUpdate(val size: Size) : Eff()
        data class UploadImage(
            val image: ImageContainer,
            val remoteViewSize: Size
        ) : Eff()

        data class UploadPaths(
            val paths: List<Path>
        ) : Eff()

        data class NotifyClear(val saveHistory: Boolean, val date: Date): Eff()
    }

    fun reducerMeasuring(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> =
        state.copy(lastReduceDurations = listOf()).let {
            measureTimedValue {
                reducer(msg, it)
            }.run {
                value.map(
                    { it.copy(lastReduceDurations = it.lastReduceDurations + duration) },
                    { it })
            }
        }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        @Suppress("UNREACHABLE_CODE")
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.UpdateLocalViewSize -> {
                    return@reducer state.reduceLocalViewSizeUpdate(msg.size)//.nativeScaled())
                }
                is Msg.UpdateRemoteViewSize -> {
                    return@state state.copy(remoteViewSize = msg.size)
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
                is Msg.NewDragPoint -> {
                    return@reducer state.reduceDragPoint(msg.point)
                }
                Msg.EndDrag -> {
                    return@state state.copyEndPath()
                }
                is Msg.UpdatePaths -> {
                    return@state state.copy(remotePaths = msg.paths)
                }
                Msg.Undo -> {
                    if (!state.undoAvailable) throw IllegalStateException("undo unavailable")
                    return@reducer state.reducePathsHistoryIndexDiff(diff = -1)
                }
                Msg.Redo -> {
                    if (!state.redoAvailable) throw IllegalStateException("redo unavailable")
                    return@reducer state.reducePathsHistoryIndexDiff(diff = +1)
                }
                is Msg.SelectBrush -> {
                    return@state state.copy(selectedBrush = msg.brush)
                }
                is Msg.LocalClear -> {
                    return@reducer state.copyClear(msg.saveHistory) toSetOf
                        Eff.NotifyClear(msg.saveHistory, Date())
                }
                is Msg.RemoteClear -> {
                    return@state state.copyClear(msg.saveHistory, msg.date)
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
        this toSetOf if (image != null && remoteViewSize != null) {
            Eff.UploadImage(image, remoteViewSize)
        } else {
            null
        }

    private fun State.reduceSendPathsUpdate() =
        this toSetOf Eff.UploadPaths(localPaths)

    private fun State.copyStartPath(point: Point): State =
        copy(
            pathsHistory = pathsHistory.subList(0, pathsHistoryIndex + 1),
            drawingPaths = listOf(
                Path(
                    listOf(point),
                    currentColor,
                    lineWidth,
                    Date(),
                )
            )
        )

    private fun State.copyEndPath(): State =
        if (drawingPaths.isNotEmpty()) {
            copy(
                pathsHistory = pathsHistory + listOf(currentPaths),
                drawingPaths = listOf(),
            ).copyPathsHistoryIndexDiff(diff = +1)
        } else this

    private fun State.copyPathsHistoryIndexDiff(diff: Int): State =
        copy(pathsHistoryIndex = pathsHistoryIndex + diff)

    private fun State.reducePathsHistoryIndexDiff(diff: Int): Pair<State, Set<Eff>> =
        copyPathsHistoryIndexDiff(diff).reduceSendPathsUpdate()

    private fun State.reduceErasePoint(point: Point): Pair<State, Set<Eff>> {
        val filtered = currentPaths.filter { path ->
            path.points.any {
                it.intersects(point, path.lineWidth + 4)
            }
        }
        if (filtered.count() != currentPaths.count()) {
            println("wasd")
        }
        return if (filtered.count() != currentPaths.count()) {
            copy(
                pathsHistory = pathsHistory + listOf(filtered),
            ).copyPathsHistoryIndexDiff(diff = +1)
                .reduceSendPathsUpdate()
        } else {
            this.withEmptySet()
        }
    }

    private fun State.reduceDragPoint(point: Point): Pair<State, Set<Eff>> {
        if (drawingConverter == null) {
            return this.withEmptySet()
        }
        val normalizedPoint = drawingConverter.normalize(point)
        return when (selectedBrush) {
            State.Brush.Pen -> reduceAddPoint(normalizedPoint)
            State.Brush.Eraser -> reduceErasePoint(normalizedPoint)
        }
    }

    private fun State.reduceAddPoint(point: Point): Pair<State, Set<Eff>> {
        if (drawingPaths.isEmpty()) {
            return copyStartPath(point).withEmptySet()
        }
        val newDrawingPaths = drawingPaths.toMutableList()
        val last = drawingPaths.last()
        if (last.points.count() < 70) {
            newDrawingPaths[drawingPaths.lastIndex] =
                last.copy(
                    points = last.points + point
                )
        } else {
            newDrawingPaths.add(
                last.copy(
                    points = last.points.takeLast(2) + listOf(point),
                    date = Date(),
                )
            )
        }
        return copy(drawingPaths = newDrawingPaths).reduceSendPathsUpdate()
    }

    internal fun State.copyClear(saveHistory: Boolean, date: Date? = null) =
        copy(
            drawingPaths = drawingPaths.newer(date),
            pathsHistory = if (saveHistory)
                pathsHistory + listOf(pathsHistory.last().newer(date))
            else
                listOf(listOf()),
            pathsHistoryIndex = if (saveHistory) pathsHistory.count() else 0,
            remotePaths = remotePaths.newer(date),
        )

    private fun List<Path>.newer(date: Date?): List<Path> =
        if (date != null) filter { it.date > date } else listOf()

    private fun Size.nativeScaled(): Size =
        Size(width * Platform.nativeScale, height * Platform.nativeScale)
}
