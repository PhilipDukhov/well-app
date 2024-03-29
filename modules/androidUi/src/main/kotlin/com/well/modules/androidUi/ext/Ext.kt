package com.well.modules.androidUi.ext

import com.well.modules.models.Color
import com.well.modules.models.Point
import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

fun Size(size: Float): Size = Size(size, size)

val Size.aspectRatio: Float
    get() = width / height

fun Color.toColor() = androidx.compose.ui.graphics.Color(argb)

fun Point.toOffset() = Offset(x, y)

fun Offset.denormalize(size: Size): Offset =
    Offset(x * size.width, y * size.height)

fun Modifier.backgroundKMM(
    color: Color,
    shape: Shape = RectangleShape,
) = background(color.toColor(), shape)

fun Modifier.borderKMM(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape,
) = border(width, color.toColor(), shape)

@Composable
fun TextKMM(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
) = Text(
    text = text,
    modifier = modifier,
    color = color?.toColor() ?: androidx.compose.ui.graphics.Color.Unspecified,
    fontSize = fontSize,
    fontStyle = fontStyle,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
    letterSpacing = letterSpacing,
    textDecoration = textDecoration,
    textAlign = textAlign,
    lineHeight = lineHeight,
    overflow = overflow,
    softWrap = softWrap,
    maxLines = maxLines,
    onTextLayout = onTextLayout,
    style = style,
)

fun Modifier.heightPlusBottomSystemBars(height: Dp) =
    composed {
        height(
            height + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
        )
    }

val WindowInsets.Companion.isImeVisible: Boolean
    @Composable
    get() {
        val density = LocalDensity.current
        val ime = this.ime
        return remember {
            derivedStateOf {
                ime.getBottom(density) > 0
            }
        }.value
    }

fun Modifier.visibility(visible: Boolean) =
    alpha(if (visible) 1F else 0F)
        .zIndex(if (visible) 1F else 0F)

fun Modifier.thenOrNull(other: Modifier?) = other?.let { then(it) } ?: this

fun Modifier.thenIf(condition: Boolean, other: Modifier) = if (condition) then(other) else this

fun Dp.toPx(density: Density) = with(density) { toPx() }

val DisplayMetrics.widthDp get() = (widthPixels / density).dp

fun LayoutCoordinates.findRoot(): LayoutCoordinates {
    var root = this
    var parent = root.parentLayoutCoordinates
    while (parent != null) {
        root = parent
        parent = root.parentLayoutCoordinates
    }
    return root
}