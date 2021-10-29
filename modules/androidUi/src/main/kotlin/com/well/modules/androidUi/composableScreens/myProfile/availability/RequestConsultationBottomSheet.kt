package com.well.modules.androidUi.composableScreens.myProfile.availability

import com.well.modules.androidUi.customViews.ActionButton
import com.well.modules.androidUi.customViews.BottomSheetDialog
import com.well.modules.androidUi.customViews.InactiveOverlay
import com.well.modules.androidUi.customViews.clickable
import com.well.modules.androidUi.ext.backgroundKMM
import com.well.modules.androidUi.ext.borderKMM
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.myProfile.currentUserAvailability.RequestConsultationFeature.Msg
import com.well.modules.features.myProfile.currentUserAvailability.RequestConsultationFeature.State
import com.well.modules.models.Availability
import com.well.modules.models.Color
import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import com.well.modules.features.myProfile.currentUserAvailability.RequestConsultationFeature as Feature
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun RequestConsultationBottomSheet(
    state: State,
    listener: (Msg) -> Unit,
) {
    BottomSheetDialog(onDismissRequest = { listener(Msg.Close) }) {
        Box {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Content(state, listener)
            }
            Overlay(state.status)
        }
    }
}

@Composable
private fun BoxScope.Overlay(status: State.Status) {
    when (status) {
        State.Status.Loading,
        State.Status.Loaded,
        -> Unit
        State.Status.Processing -> {
            InactiveOverlay()
        }
        State.Status.Booked -> {
            InactiveOverlay(showActivityIndicator = false) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .aspectRatio(1f),
                    tint = Color.Green.toColor()
                )
            }
        }
        is State.Status.BookingFailed -> InactiveOverlay(showActivityIndicator = false) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Feature.Strings.bookingFailed, style = MaterialTheme.typography.body1)
                Text(status.reason, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Content(
    state: State,
    listener: (Msg) -> Unit,
) {
    Spacer(Modifier.height(16.dp))
    Text(
        Feature.Strings.title,
        style = MaterialTheme.typography.subtitle2,
    )
    Spacer(Modifier.height(40.dp))
    if (state.status == State.Status.Loading) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.1f)
                .aspectRatio(1f)
        )
        Spacer(Modifier.height(40.dp))
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Availabilities(state.availabilitiesByDay) {
                listener(Msg.Book(it))
            }
        }
    }
}

@Composable
private fun Availabilities(
    availabilities: List<Pair<LocalDate, List<Availability>>>,
    onBookNow: (Availability) -> Unit,
) {
    val (selectedDayIndex, setSelectedDayIndex) = rememberSaveable(availabilities) {
        mutableStateOf(0)
    }
    val (selectedTimeIndex, setSelectedTimeIndex) = rememberSaveable(selectedDayIndex) {
        mutableStateOf<Int?>(null)
    }
    val scope = rememberCoroutineScope()
    val topRowState = rememberLazyListState()
    Column(
        verticalArrangement = Arrangement.spacedBy(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BookRow(
            state = topRowState,
            items = remember(availabilities) { availabilities.map { it.first } },
            selectedIndex = selectedDayIndex,
            setSelectedIndex = setSelectedDayIndex,
            itemText = { it.localizedDayAndShortMonth(separator = "\n") },
            textStyle = MaterialTheme.typography.body2,
            aspectRatio = 1f,
            fillParentMaxWidthPart = 0.2f
        )
        Divider(Modifier.padding(horizontal = 16.dp))
        BookRow(
            items = remember(selectedDayIndex) { availabilities[selectedDayIndex].second },
            selectedIndex = selectedTimeIndex,
            setSelectedIndex = { selectedIndex ->
                Napier.wtf("setSelectedTimeIndex ${topRowState.layoutInfo.visibleItemsInfo.none { it.index == selectedDayIndex }}")
                setSelectedTimeIndex(selectedIndex)
                if (topRowState.layoutInfo.visibleItemsInfo.none { it.index == selectedDayIndex }) {
                    scope.launch {
                        try {
                            topRowState.animateScrollToItem(selectedDayIndex)
                        } catch (t: Throwable) {
                        }
                    }
                }
            },
            itemText = { it.startTime.toString() },
            textStyle = MaterialTheme.typography.body1,
            aspectRatio = 2f,
            fillParentMaxWidthPart = 0.33f
        )
    }
    ActionButton(
        state = selectedTimeIndex,
        onClick = { unwrappedSelectedTimeIndex ->
            scope.launch {
                if (topRowState.layoutInfo.visibleItemsInfo.any { it.index == 0 }) {
                    topRowState.animateScrollToItem(availabilities.count() - 1)
                } else {
                    topRowState.scrollToItem(0)
                }
            }
            onBookNow(availabilities[selectedDayIndex].second[unwrappedSelectedTimeIndex])
        },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(Feature.Strings.bookNow)
    }
    // to apply padding by verticalArrangement
    Spacer(Modifier)
}

@Composable
private fun <T> BookRow(
    state: LazyListState = rememberLazyListState(),
    items: List<T>,
    selectedIndex: Int?,
    setSelectedIndex: (Int) -> Unit,
    itemText: (T) -> String,
    textStyle: TextStyle,
    aspectRatio: Float,
    fillParentMaxWidthPart: Float,
) {
    LazyRow(
        state = state,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(items) { i, item ->
            val selected = i == selectedIndex
            val shape = RoundedCornerShape(14.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillParentMaxWidth(fillParentMaxWidthPart)
                    .aspectRatio(aspectRatio)
                    .clip(shape)
                    .borderKMM(
                        if (selected) 0.dp else 2.dp,
                        color = Color.LightGray,
                        shape = shape,
                    )
                    .backgroundKMM(
                        if (selected) Color.Green else Color.Transparent,
                    )
                    .clickable {
                        Napier.wtf("clickable $i")
                        setSelectedIndex(i)
                    }
            ) {
                Text(
                    itemText(item),
                    color = (if (selected) Color.White else Color.DarkGrey).toColor(),
                    textAlign = TextAlign.Center,
                    style = textStyle,
                )
            }
        }
    }
}