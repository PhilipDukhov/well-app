package com.well.modules.androidUi.composableScreens.myProfile.availability

import com.well.modules.androidUi.components.ActionButton
import com.well.modules.androidUi.components.BottomSheetDialog
import com.well.modules.androidUi.components.InactiveOverlay
import com.well.modules.androidUi.components.SelectableButton
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.RequestConsultationFeature.Msg
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.RequestConsultationFeature.State
import com.well.modules.models.BookingAvailability
import com.well.modules.models.Color
import com.well.modules.models.date.dateTime.localizedDayAndShortMonth
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.RequestConsultationFeature as Feature
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                RequestConsultationView(state, listener)
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
            InactiveOverlay(showActivityIndicator = true)
        }
        State.Status.Booked -> {
            InactiveOverlay() {
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
        is State.Status.BookingFailed -> InactiveOverlay() {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(Feature.Strings.bookingFailed, style = MaterialTheme.typography.body1)
                Text(status.reason, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun RequestConsultationView(
    state: State,
    listener: (Msg) -> Unit,
) {
    Spacer(Modifier.height(16.dp))
    Text(
        Feature.Strings.title,
        style = MaterialTheme.typography.subtitle2,
    )
    Spacer(Modifier.height(40.dp))
    when {
        state.status == State.Status.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.1f)
                    .aspectRatio(1f)
            )
            Spacer(Modifier.height(40.dp))
        }
        state.availabilitiesByDay.isEmpty() -> {
            Text(Feature.Strings.hasNoConsultations, style = MaterialTheme.typography.body1)
            Spacer(Modifier.height(40.dp))
        }
        else -> {
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
}

@Composable
private fun Availabilities(
    availabilities: List<Pair<LocalDate, List<BookingAvailability>>>,
    onBookNow: (BookingAvailability) -> Unit,
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
        ProvideTextStyle(MaterialTheme.typography.body2) {
            BookRow(
                state = topRowState,
                items = remember(availabilities) { availabilities.map { it.first } },
                selectedIndex = selectedDayIndex,
                setSelectedIndex = setSelectedDayIndex,
                itemText = { it.localizedDayAndShortMonth(separator = "\n") },
                aspectRatio = 1f,
                fillParentMaxWidthPart = 0.2f
            )
        }
        Divider(Modifier.padding(horizontal = 16.dp))

        ProvideTextStyle(MaterialTheme.typography.body1) {
            BookRow(
                items = remember(selectedDayIndex) { availabilities[selectedDayIndex].second },
                selectedIndex = selectedTimeIndex,
                setSelectedIndex = { selectedIndex ->
                    setSelectedTimeIndex(selectedIndex)
                    if (topRowState.layoutInfo.visibleItemsInfo.none { it.index == selectedDayIndex }) {
                        scope.launch {
                            try {
                                topRowState.animateScrollToItem(selectedDayIndex)
                            } catch (_: Throwable) {
                            }
                        }
                    }
                },
                itemText = { it.startTime.toString() },
                aspectRatio = 2f,
                fillParentMaxWidthPart = 0.33f
            )
        }
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
            SelectableButton(
                selected = i == selectedIndex,
                onClick = {
                    setSelectedIndex(i)
                },
                modifier = Modifier
                    .fillParentMaxWidth(fillParentMaxWidthPart)
                    .aspectRatio(aspectRatio)
            ) {
                Text(
                    itemText(item),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}