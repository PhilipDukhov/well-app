package com.well.androidApp.ui.composableScreens.call.drawing

import androidx.compose.animation.asDisposableClock
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.pressIndicatorGestureFilter
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.AmbientAnimationClock
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.dp
import com.well.androidApp.ui.composableScreens.call.drawing.SliderDefaults.InactiveTrackColorAlpha

@Composable
fun WidthSlider(
    state: MutableState<Float>,
    modifier: Modifier = Modifier,
    thumbRadiusRange: ClosedFloatingPointRange<Float> = 1f..50f,
    onValueChangeEnd: () -> Unit = {},
    mainThumbColor: Color = MaterialTheme.colors.primary,
    previewThumbColor: Color,
    inactiveTrackColor: Color = MaterialTheme.colors.primary.copy(alpha = InactiveTrackColorAlpha),
) {
    val interactionState = remember { InteractionState() }
    val clock = AmbientAnimationClock.current.asDisposableClock()
    val onValueChange: (Float) -> Unit = {
        state.value = it
    }
    val position = remember(thumbRadiusRange) {
        SliderPosition(state.value, thumbRadiusRange, clock, onValueChange)
    }
    position.onValueChange = onValueChange
    position.scaledValue = state.value
    BoxWithConstraints(modifier.sliderSemantics(state.value, position, onValueChange, thumbRadiusRange)) {
        val maxPy = constraints.maxHeight.toFloat()
        val pressPadding = 45.dp / 2 - ThumbRadius
        val pressPaddingPx = with(LocalDensity.current) { pressPadding.toPx() }
        position.setBounds(pressPaddingPx, maxPy - pressPaddingPx)

        val press = Modifier.pressIndicatorGestureFilter(
            onStart = { pos ->
                position.holder.snapTo(maxPy - pos.y)
                interactionState.addInteraction(Interaction.Pressed, pos)
            },
            onStop = {
                onValueChangeEnd()
                interactionState.removeInteraction(Interaction.Pressed)
            },
            onCancel = {
                interactionState.removeInteraction(Interaction.Pressed)
            }
        )

        val drag = Modifier.draggable(
            orientation = Orientation.Vertical,
            interactionState = interactionState,
            onDragStopped = { onValueChangeEnd() },
            startDragImmediately = position.holder.isRunning,
            onDrag = {
                println("$it")
                position.holder.snapTo(position.holder.value - it)
            }
        )
        val coerced = state.value.coerceIn(position.startValue, position.endValue)
        val fraction = calcFraction(position.startValue, position.endValue, coerced)
        val heightDp = with(LocalDensity.current) {
            maxPy.toDp()
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val thumbSize = ThumbRadius * 2
            val center = Modifier.align(Alignment.TopStart)
            val offset = (heightDp - thumbSize - pressPadding * 2) * (1 - fraction)
            Box(
                modifier = Modifier
                    .then(press)
                    .then(drag)
                    .padding(pressPadding)
            ) {
                Track(
                    modifier = center.fillMaxHeight(),
                    inactiveTrackColor,
                    interactionState,
                )
                Box(
                    modifier = center
                        .offset(y = offset)
                ) {
                    val elevation = if (
                        Interaction.Pressed in interactionState || Interaction.Dragged in interactionState
                    ) {
                        ThumbPressedElevation
                    } else {
                        ThumbDefaultElevation
                    }
                    Surface(
                        shape = CircleShape,
                        color = mainThumbColor,
                        elevation = elevation,
                        modifier = Modifier
                            .focusable(interactionState = interactionState)
                            .indication(
                                interactionState = interactionState,
                                indication = rememberRipple(
                                    bounded = false,
                                    radius = ThumbRippleRadius
                                )
                            )
                    ) {
                        Spacer(Modifier.preferredSize(thumbSize))
                    }
                }
            }
            if (interactionState.value.isNotEmpty()) {
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

object SliderDefaults {
    const val InactiveTrackColorAlpha = 0.24f
}

enum class InteractionStateWrapper {
    Interacting,
    Idle,
    ;
}

fun InteractionStateWrapper(interactionState: InteractionState) =
    if (interactionState.value.isEmpty()) InteractionStateWrapper.Idle else InteractionStateWrapper.Interacting

// Internal to be referred to in tests
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
    interactionState: InteractionState,
) {
    val transition = updateTransition(InteractionStateWrapper(interactionState))
    val topWidth by transition.animateFloat {
        when (it) {
            InteractionStateWrapper.Idle -> {
                TrackWidth
            }
            InteractionStateWrapper.Interacting -> {
                TriangleWidth
            }
        }
    }
    val bottomWidth by transition.animateFloat {
        when (it) {
            InteractionStateWrapper.Idle -> {
                TrackWidth
            }
            InteractionStateWrapper.Interacting -> {
                0F
            }
        }
    }
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
) =
    lerp(a2, b2, calcFraction(a1, b1, x1))

// Calculate the 0..1 fraction that `pos` value represents between `a` and `b`
private fun calcFraction(
    a: Float,
    b: Float,
    pos: Float
) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)

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

private class SliderPosition(
    initial: Float = 0f,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    animatedClock: AnimationClockObservable,
    var onValueChange: (Float) -> Unit
) {

    val startValue: Float = valueRange.start
    val endValue: Float = valueRange.endInclusive

    var scaledValue: Float = initial
        set(value) {
            val scaled = scale(startValue, endValue, value, startPy, endPy)
            // floating point error due to rescaling
            if ((scaled - holder.value) > floatPointMistakeCorrection) {
                holder.snapTo(scaled)
            }
        }

    private val floatPointMistakeCorrection = (valueRange.endInclusive - valueRange.start) / 100

    private var endPy = Float.MAX_VALUE
    private var startPy = Float.MIN_VALUE

    fun setBounds(
        min: Float,
        max: Float
    ) {
        if (startPy == min && endPy == max) return
        val newValue = scale(startPy, endPy, holder.value, min, max)
        startPy = min
        endPy = max
        holder.setBounds(min, max)
        holder.snapTo(newValue)
    }

    val holder =
        CallbackBasedAnimatedFloat(
            scale(startValue, endValue, initial, startPy, endPy),
            animatedClock
        ) { onValueChange(scale(startPy, endPy, it, startValue, endValue)) }
}

private class CallbackBasedAnimatedFloat(
    initial: Float,
    clock: AnimationClockObservable,
    var onValue: (Float) -> Unit
) : AnimatedFloat(clock) {

    override var value = initial
        set(value) {
            onValue(value)
            field = value
        }
}

private fun lerp(
    start: Float,
    stop: Float,
    fraction: Float
) =
    (start * (1 - fraction) + stop * fraction)