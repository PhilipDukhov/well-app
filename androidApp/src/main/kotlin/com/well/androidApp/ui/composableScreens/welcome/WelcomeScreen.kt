package com.well.androidApp.ui.composableScreens.welcome

import com.well.androidApp.ui.customViews.ActionButton
import com.well.androidApp.ui.customViews.ActionButtonStyle
import com.well.androidApp.ui.customViews.gesturesDisabled
import com.well.androidApp.ui.ext.aspectRatio
import com.well.androidApp.ui.ext.backgroundKMM
import com.well.androidApp.ui.ext.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.puerh.welcome.WelcomeFeature.Msg
import com.well.sharedMobile.puerh.welcome.WelcomeFeature.State
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.math.BigDecimal

@Composable
fun WelcomeScreen(
    state: State,
    listener: (Msg) -> Unit,
) = ConstraintLayout(
    modifier = Modifier
        .fillMaxSize()
        .backgroundKMM(Color.DarkBlue)
) {
    val (imagesRef, textsRef) = createRefs()
    val context = LocalContext.current
    val lazyRowState = rememberLazyListState()
    var rowWidth by remember { mutableStateOf(0) }
    val cornerRadius = 20.dp
    LazyRow(
        state = lazyRowState,
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { rowWidth = it.width }
            .constrainAs(imagesRef) {
                top.linkTo(parent.top)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
                bottom.linkTo(textsRef.top, margin = -cornerRadius)
                height = Dimension.fillToConstraints
            }
            .background(Color.RadicalRed.toColor())
            .gesturesDisabled()
    ) {
        itemsIndexed(state.descriptions) { i, _ ->
            val drawableId = remember {
                context.resources.getIdentifier(
                    "welcome_${i + 1}",
                    "drawable",
                    context.packageName
                )
            }
            val painter = painterResource(id = drawableId)
            Box {
                Image(
                    painter,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .fillMaxHeight()
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .backgroundKMM(
                Color.White,
                shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
            )
            .navigationBarsPadding()
            .constrainAs(textsRef) {
                bottom.linkTo(parent.bottom)
                absoluteLeft.linkTo(parent.absoluteLeft)
                absoluteRight.linkTo(parent.absoluteRight)
            }
    ) {
        Spacer(modifier = Modifier.height(cornerRadius))
        Text(
            text = state.title,
            style = MaterialTheme.typography.h4,
            color = Color.DarkBlue.toColor(),
        )
        val pagerState = rememberPagerState()
        val pagePart = remember(pagerState.currentPage, pagerState.currentPageOffset) {
            pagerState.currentPage + pagerState.currentPageOffset
        }
        LaunchedEffect(pagePart) {
            val divideAndRemainder = BigDecimal.valueOf(pagePart.toDouble())
                .divideAndRemainder(BigDecimal.ONE)

            lazyRowState.scrollToItem(
                divideAndRemainder[0].toInt().coerceIn(0, state.descriptions.count() - 1),
                (divideAndRemainder[1].toFloat() * rowWidth).roundToInt(),
            )
        }
        val textComposable = @Composable { text: String ->
            Text(
                text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .drawWithContent {}
            ) {
                state.descriptions.forEach { text ->
                    textComposable(text)
                }
            }
            HorizontalPager(
                count = state.descriptions.count(),
                state = pagerState,
                modifier = Modifier
                    .matchParentSize()
            ) { page ->
                Box(
                    Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    textComposable(state.descriptions[page])
                }
            }
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = Color.LightBlue.toColor(),
            modifier = Modifier
                .padding(16.dp),
        )
        val scope = rememberCoroutineScope()
        ActionButton(
            onClick = {
                scope.launch {
                    val nextPage = pagerState.currentPage + 1
                    if (nextPage < pagerState.pageCount) {
                        pagerState.animateScrollToPage(nextPage)
                    } else {
                        listener(Msg.Continue)
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 26.dp)
        ) {
            Text("Next")
        }
        Spacer(Modifier.height(16.dp))
        val visible = remember(pagePart, pagerState.pageCount) {
            pagePart.roundToInt() + 1 < pagerState.pageCount
        }
        val alpha: Float by animateFloatAsState(if (visible) 1f else 0f)
        ActionButton(
            onClick = { listener(Msg.Continue) },
            style = ActionButtonStyle.White,
            modifier = Modifier
                .alpha(alpha)
        ) {
            Text("skip all")
        }
        Spacer(Modifier.height(16.dp))
    }
}