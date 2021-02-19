package com.well.androidApp.ui.composableScreens.πExt

import android.util.DisplayMetrics
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AmbientTextStyle
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.zIndex
import com.well.serverModels.Color
import com.well.serverModels.Point
import dev.chrisbanes.accompanist.insets.AmbientWindowInsets
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.toPaddingValues

fun Size(size: Float): Size = Size(size, size)

fun Color.toColor() = androidx.compose.ui.graphics.Color(argb)

fun Point.toOffset() = Offset(x, y)

fun Offset.denormalize(constraints: Constraints): Offset =
    Offset(x * constraints.minWidth, y * constraints.minHeight)

fun Modifier.backgroundKMM(
    color: Color,
    shape: Shape = RectangleShape
) = background(color.toColor(), shape)

fun Modifier.borderKMM(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape
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
    style: TextStyle = LocalTextStyle.current
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
            height + LocalWindowInsets.current.systemBars.toPaddingValues()
                .calculateBottomPadding()
        )
    }

fun Modifier.visibility(visible: Boolean) =
    alpha(if (visible) 1F else 0F)
        .zIndex(if (visible) 1F else 0F)

fun Modifier.thenOrNull(other: Modifier?) = other?.let { then(it) } ?: this

fun Dp.toPx(density: Density) = with(density) { toPx() }

@Composable
fun Image(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) = androidx.compose.foundation.Image(
    painter = painter,
    contentDescription = contentDescription,
    modifier = modifier,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
)

val DisplayMetrics.widthDp get() = (widthPixels / density).dp