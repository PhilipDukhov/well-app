package com.well.androidApp.ui.composableScreens.call.drawing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.google.android.material.math.MathUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

@Composable
fun WidthSlider(
    state: MutableState<Float>,
    modifier: Modifier = Modifier,
    thumbRadiusRange: ClosedFloatingPointRange<Float> = 1f..50f,
    onValueChangeEnd: () -> Unit = {},
    mainThumbColor: Color = MaterialTheme.colors.primary,
    previewThumbColor: Color,
    inactiveTrackColor: Color = MaterialTheme.colors.primary.copy(alpha = 0.24f),
) {
    val scope = rememberCoroutineScope()
    val onValueChange: (Float) -> Unit = {
        state.value = it
    }
    val position = remember(state.value, thumbRadiusRange, scope) {
        SliderPosition(0f, thumbRadiusRange, scope, onValueChange)
    }
    position.onValueChange = onValueChange
    val interactionSource = remember { MutableInteractionSource() }
    BoxWithConstraints(
        modifier.sliderSemantics(
            state.value,
            position,
            onValueChange,
            thumbRadiusRange
        )
    ) {
        val maxHeight = constraints.maxHeight.toFloat()
        val pressPadding = 45.dp / 2 - ThumbRadius
        val pressPaddingPx = with(LocalDensity.current) { pressPadding.toPx() }
        position.setBounds(pressPaddingPx, maxHeight - pressPaddingPx)

        position.snapToScaled(state.value)

        val press = Modifier.pointerInput(maxHeight) {
            detectTapGestures(
                onPress = { pos ->
                    position.snapTo(maxHeight - pos.y)
                    val interaction = PressInteraction.Press(pos)
                    coroutineScope {
                        launch {
                            interactionSource.emit(interaction)
                        }
                    }
                    val success = tryAwaitRelease()
                    if (success) onValueChangeEnd()
                    coroutineScope {
                        launch {
                            interactionSource.emit(PressInteraction.Release(interaction))
                        }
                    }
                }
            )
        }
        val drag = Modifier.draggable(
            orientation = Orientation.Vertical,
            interactionSource = interactionSource,
            onDragStopped = { onValueChangeEnd() },
            startDragImmediately = position.holder.isRunning,
            state = rememberDraggableState {
                position.snapToDiff(it)
            }
        )
        val coerced = state.value.coerceIn(position.startValue, position.endValue)
        val fraction = calcFraction(position.startValue, position.endValue, coerced)
        val maxHeightDp = with(LocalDensity.current) {
            maxHeight.toDp()
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val thumbSize = ThumbRadius * 2
            val center = Modifier.align(Alignment.TopStart)
            val offset = (maxHeightDp - thumbSize - pressPadding * 2) * (1 - fraction)
            val isPressed by interactionSource.collectIsPressedAsState()
            val isDragged by interactionSource.collectIsDraggedAsState()
            Box(
                modifier = Modifier
                    .then(press)
                    .then(drag)
                    .padding(pressPadding)
            ) {
                Track(
                    modifier = center.fillMaxHeight(),
                    inactiveTrackColor,
                    isDragged,
                )
                Box(center.offset(y = offset)) {
                    val hasInteraction = isPressed || isDragged
                    val elevation = if (hasInteraction) {
                        ThumbPressedElevation
                    } else {
                        ThumbDefaultElevation
                    }
                    Surface(
                        shape = CircleShape,
                        color = mainThumbColor,
                        elevation = elevation,
                        modifier = Modifier
                            .focusable(interactionSource = interactionSource)
                            .indication(
                                interactionSource = interactionSource,
                                indication = rememberRipple(
                                    bounded = false,
                                    radius = ThumbRippleRadius
                                )
                            )
                    ) {
                        Spacer(Modifier.size(thumbSize, thumbSize))
                    }
                }
            }
            if (isDragged) {
                val size = (thumbRadiusRange.start +
                    (thumbRadiusRange.endInclusive - thumbRadiusRange.start) * fraction).dp
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(
                            x = size / 2 - thumbRadiusRange.endInclusive.dp,
                            y = offset + pressPadding + thumbSize / 2 - size / 2,
                        )
                ) {
                    Surface(
                        shape = CircleShape,
                        color = previewThumbColor,
                        modifier = Modifier
                    ) {
                        Spacer(
                            Modifier.size(
                                size
                            )
                        )
                    }
                }
            }
        }
    }
}

internal val ThumbRadius = 10.dp
private val ThumbRippleRadius = 24.dp
private val ThumbDefaultElevation = 1.dp
private val ThumbPressedElevation = 6.dp
private const val TrackWidth = 4F
private val TriangleWidth = (ThumbRadius.value * 6F)

@Composable
private fun Track(
    modifier: Modifier,
    inactiveColor: Color,
    isDragged: Boolean,
) {
    val topWidth: Float by animateFloatAsState(if (isDragged) TriangleWidth else TrackWidth)
    val bottomWidth: Float by animateFloatAsState(if (isDragged) 0F else TrackWidth)
    Canvas(modifier) {
        drawPath(
            Path().apply {
                val center = TriangleWidth / 2
                moveTo(x = center - bottomWidth / 2, y = size.height)
                lineTo(x = center + bottomWidth / 2, y = size.height)
                lineTo(x = center + topWidth / 2, y = 0F)
                lineTo(x = center - topWidth / 2, y = 0F)
                close()
            },
            inactiveColor,
        )
    }
}

// Scale x1 from a1..b1 range to a2..b2 range
private fun scale(
    a1: Float,
    b1: Float,
    x1: Float,
    a2: Float,
    b2: Float
) = lerp(a2, b2, calcFraction(a1, b1, x1))

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(
    a: Float,
    b: Float,
    pos: Float
) = (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

private fun Modifier.sliderSemantics(
    value: Float,
    position: SliderPosition,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
): Modifier {
    val coerced = value.coerceIn(position.startValue, position.endValue)
    return semantics(mergeDescendants = true) {
        setProgress(
            action = { targetValue ->
                val newValue = targetValue.coerceIn(position.startValue, position.endValue)
                // This is to keep it consistent with AbsSeekbar.java: return false if no
                // change from current.
                if (newValue == coerced) {
                    false
                } else {
                    onValueChange(newValue)
                    true
                }
            }
        )
    }.progressSemantics(value, valueRange)
}

/**
 * Internal state for [Slider] that represents the Slider value, its bounds and optional amount of
 * steps evenly distributed across the Slider range.
 *
 * @param initial initial value for the Slider when created. If outside of range provided,
 * initial position will be coerced to this range
 * @param valueRange range of values that Slider value can take
 */
private class SliderPosition(
    private val initial: Float = 0f,
    val valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    /*@IntRange(from = 0)*/
    val scope: CoroutineScope,
    var onValueChange: (Float) -> Unit
) {

    private val mutex = Mutex()
    val startValue: Float = valueRange.start
    val endValue: Float = valueRange.endInclusive

    private val floatPointMistakeCorrection = (valueRange.endInclusive - valueRange.start) / 100

    private var endPx = Float.MAX_VALUE
    private var startPx = Float.MIN_VALUE

    fun setBounds(
        min: Float,
        max: Float
    ) {
        if (startPx == min && endPx == max) return
        if (!::holder.isInitialized) {
            startPx = min
            endPx = max
            holder = Animatable(scale(startValue, endValue, initial, startPx, endPx))
            return
        }
        updateHolder {
            val newValue = scale(startPx, endPx, holder.value, min, max)
            startPx = min
            endPx = max
            holder.updateBounds(min, max)
            snapTo(newValue)
        }
    }

    lateinit var holder: Animatable<Float, AnimationVector1D>

    fun snapToScaled(newValue: Float) {
        // floating point error due to rescaling
        updateHolder {
            val scaled = scale(startValue, endValue, newValue, startPx, endPx)
            if ((scaled - holder.value) > floatPointMistakeCorrection) {
                holder.snapTo(scaled)
            }
        }
    }

    fun snapToDiff(diff: Float) {
        updateHolder {
            holder.snapTo(holder.value - diff)
            onHolderValueUpdated(holder.value)
        }
    }

    fun snapTo(newValue: Float) {
        updateHolder {
            holder.snapTo(newValue)
            onHolderValueUpdated(holder.value)
        }
    }

    fun updateHolder(block: suspend () -> Unit) {
        scope.launch {
            mutex.lock()
            block()
            mutex.unlock()
        }
    }

    val onHolderValueUpdated: (value: Float) -> Unit = {
        onValueChange(scale(startPx, endPx, it, startValue, endValue))
    }
}