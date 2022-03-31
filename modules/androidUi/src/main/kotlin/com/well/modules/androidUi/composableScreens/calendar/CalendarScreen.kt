package com.well.modules.androidUi.composableScreens.calendar

import com.well.modules.androidUi.customViews.CalendarMonthView
import com.well.modules.androidUi.customViews.CalendarTitleScope
import com.well.modules.androidUi.customViews.ProfileImage
import com.well.modules.androidUi.customViews.gradientBackground
import com.well.modules.androidUi.ext.minus
import com.well.modules.androidUi.ext.plus
import com.well.modules.androidUi.ext.start
import com.well.modules.androidUi.ext.toColor
import com.well.modules.features.calendar.calendarFeature.CalendarFeature.Msg
import com.well.modules.features.calendar.calendarFeature.CalendarFeature.State
import com.well.modules.models.Color
import com.well.modules.models.Meeting
import com.well.modules.models.MeetingViewModel
import com.well.modules.models.date.dateTime.localizedRelatedDescription
import com.well.modules.utils.kotlinUtils.ifTrueOrNull
import com.well.modules.utils.viewUtils.CalendarMonthViewColors
import com.well.modules.utils.viewUtils.Gradient
import com.well.modules.features.calendar.calendarFeature.CalendarFeature as Feature
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun CalendarScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .gradientBackground(Gradient.Main)
            .padding(horizontal = 15.dp)
            .statusBarsPadding()
    ) {
        CalendarMonthView(
            state = state.infoState,
            title = { TitleView(state) },
            colors = CalendarMonthViewColors.mainCalendar,
            onSelect = {
                listener(Msg.SelectDate(it))
            },
            showNextMonth = {
                listener(Msg.NextMonth)
            },
            showPrevMonth = {
                listener(Msg.PrevMonth)
            },
        )
        val (dialogMeeting, setDialogMeeting) = remember { mutableStateOf<MeetingViewModel?>(null) }
        MeetingsList(
            state.selectedItemMeetings?.let(::listOf)
                ?: state.upcomingMeetings,
            onSelectMeeting = setDialogMeeting,
            onStartCall = {
                listener(Msg.StartCall(it))
            },
            onSelectUser = {
                listener(Msg.OpenUserProfile(it))
            },
            onUpdateState = { meeting, state ->
                listener(Msg.UpdateMeetingState(meeting, state))
            },
        )
    }
}

@Composable
private fun CalendarTitleScope.TitleView(state: State) {
    CompositionLocalProvider(LocalContentColor provides Color.White.toColor()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(vertical = 30.dp)
        ) {
            Row {
                TitleOtherMonth(
                    text = state.prevMonthName,
                    moveToLeft = true,
                    onClick = ::showPrevMonth,
                )
                Text(
                    state.currentMonthName,
                    style = MaterialTheme.typography.h3,
                    modifier = Modifier.padding(horizontal = 25.dp)
                )
                TitleOtherMonth(
                    text = state.nextMonthName,
                    moveToLeft = false,
                    onClick = ::showNextMonth,
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                state.year.toString(),
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@Composable
private fun RowScope.TitleOtherMonth(
    text: String,
    moveToLeft: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = Color.White.toColor().copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .weight(1f)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(maxWidth = Int.MAX_VALUE))
                val width = minOf(placeable.width, constraints.maxWidth)
                layout(width, placeable.height) {
                    placeable.place(
                        x = if (moveToLeft) width - placeable.width else 0,
                        y = 0
                    )
                }
            }
    ) {
        Text(
            text,
            style = MaterialTheme.typography.h5,
        )
    }
}

@Composable
private fun MeetingsList(
    meetings: List<State.DayMeetings>,
    onSelectMeeting: (MeetingViewModel) -> Unit,
    onStartCall: (MeetingViewModel) -> Unit,
    onSelectUser: (MeetingViewModel) -> Unit,
    onUpdateState: (MeetingViewModel, Meeting.State) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(vertical = 15.dp)
    ) {
        meetings.forEach { dayMeetings ->
            item(key = dayMeetings.day.toString()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        dayMeetings.day.localizedRelatedDescription(),
                        style = MaterialTheme.typography.body1,
                        color = Color.White.toColor()
                    )
                }
            }
            items(dayMeetings.meetings, key = { it.id.toString() }) { meeting ->
                if (meeting.waitingExpertResolution) {
                    RequestedMeetingCard(
                        meeting = meeting,
                        onUpdateState = {
                            onUpdateState(meeting, it)
                        },
                        onSelectUser = {
                            onSelectUser(meeting)
                        },
                    )
                } else {
                    ConfirmedMeetingCard(
                        meeting = meeting,
                        onSelect = {
                            onSelectMeeting(meeting)
                        },
                        onStartCall = {
                            onStartCall(meeting)
                        },
                        onSelectUser = {
                            onSelectUser(meeting)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmedMeetingCard(
    meeting: MeetingViewModel,
    onSelect: () -> Unit,
    onSelectUser: () -> Unit,
    onStartCall: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = meeting.status == MeetingViewModel.Status.Upcoming,
                onClick = onSelect,
            )
    ) {
        Box(
            Modifier
                .padding(15.dp)
                .padding(start = 12.dp)
        ) {
            Column {
                ProvideTextStyle(value = MaterialTheme.typography.caption) {
                    if (meeting.status == MeetingViewModel.Status.Ongoing) {
                        Text(
                            Feature.Strings.now,
                            color = Color.Green.toColor(),
                        )
                    } else {
                        Text(
                            meeting.startTime.toString(),
                            textDecoration = ifTrueOrNull(meeting.status == MeetingViewModel.Status.Past) {
                                TextDecoration.LineThrough
                            },
                            color = Color.Black.toColor(),
                        )
                    }
                }
                Text(
                    meeting.title,
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.Black.toColor(),
                    modifier = Modifier.padding(top = 3.dp, bottom = 9.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            onSelectUser()
                        }
                ) {
                    ProfileImage(
                        user = meeting.otherUser,
                        modifier = Modifier
                            .padding(end = 7.dp)
                            .size(20.dp)
                    )
                    Text(
                        "with ${meeting.otherUser.fullName}",
                        style = MaterialTheme.typography.caption,
                        color = Color.LightBlue.toColor()
                    )
                }
            }
            if (meeting.status == MeetingViewModel.Status.Ongoing) {
                IconButton(
                    onClick = onStartCall,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        Icons.Rounded.Call,
                        contentDescription = null,
                        tint = Color.Green.toColor(),
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestedMeetingCard(
    meeting: MeetingViewModel,
    onSelectUser: () -> Unit,
    onUpdateState: (Meeting.State) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(
                    PaddingValues(15.dp) + PaddingValues(start = 12.dp) - ButtonDefaults.TextButtonContentPadding
                )
                .fillMaxWidth()
        ) {
            if (meeting.isExpert) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onSelectUser) {
                        Text(
                            meeting.otherUser.fullName,
                            style = MaterialTheme.typography.subtitle2,
                        )
                    }
                    Text(
                        Feature.Strings.needsYourHelp,
                        style = MaterialTheme.typography.subtitle2,
                    )
                }
            } else {
                Text(
                    meeting.title,
                    style = MaterialTheme.typography.subtitle2,
                )
            }
            Text(
                "${Feature.Strings.bookingTime}: ${meeting.startTime}",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(
                    start = ButtonDefaults.TextButtonContentPadding.start()
                )
            )

            Row {
                TextButton(onClick = {
                    onUpdateState(Meeting.State.Confirmed)
                }) {
                    Text(
                        Feature.Strings.confirm,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.MediumBlue.toColor(),
                    )
                }
                Spacer(Modifier.width(10.dp))
                TextButton(onClick = {
                    onUpdateState(Meeting.State.Rejected)
                }) {
                    Text(
                        Feature.Strings.reject,
                        style = MaterialTheme.typography.subtitle2,
                        color = Color.Pink.toColor(),
                    )
                }
            }
        }
    }
}