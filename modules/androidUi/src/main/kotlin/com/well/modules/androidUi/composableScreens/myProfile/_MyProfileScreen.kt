package com.well.modules.androidUi.composableScreens.myProfile

import com.well.modules.androidUi.R
import com.well.modules.androidUi.composableScreens.myProfile.availability.CurrentUserAvailabilityView
import com.well.modules.androidUi.composableScreens.myProfile.availability.RequestConsultationBottomSheet
import com.well.modules.androidUi.customViews.Control
import com.well.modules.androidUi.customViews.InactiveOverlay
import com.well.modules.androidUi.customViews.ModeledNavigationBar
import com.well.modules.androidUi.customViews.ProfileImage
import com.well.modules.androidUi.customViews.RatingInfoView
import com.well.modules.androidUi.customViews.ToggleFavoriteButton
import com.well.modules.androidUi.ext.findRoot
import com.well.modules.androidUi.ext.toColor
import com.well.modules.androidUi.ext.visibility
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Msg
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.State
import com.well.modules.features.myProfile.myProfileFeature.UIGroup
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature as Feature
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets

// ColumnScope unused
@Suppress("unused")
@Composable
fun ColumnScope.MyProfileScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    val (rightItemBeforeAction, setRightItemBeforeAction) = remember {
        mutableStateOf<(() -> Unit)?>(
            null
        )
    }
    val availabilityState = state.availabilityState
    var selectedTab by rememberSaveable(availabilityState != null) {
        mutableStateOf(state.tabs.first())
    }
    state.navigationBarModel?.let {
        ModeledNavigationBar(
            it.copy(
                rightItem = when (selectedTab) {
                    Feature.ProfileTab.ProfileInformation -> it.rightItem
                    else -> null
                }
            ),
            listener,
            rightItemBeforeAction = rightItemBeforeAction,
            extraContent = {
                if (state.tabs.count() > 1) {
                    TabRow(
                        selectedTabIndex = state.tabs.indexOf(selectedTab!!),
                        backgroundColor = Color.Transparent.toColor(),
                        contentColor = Color.White.toColor(),
                    ) {
                        Feature.ProfileTab.values().forEach { tab ->
                            Tab(
                                text = {
                                    Text(
                                        tab.title,
                                        style = MaterialTheme.typography.body2,
                                    )
                                },
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                            )
                        }
                    }
                    Divider()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        )
    }
    when (selectedTab) {
        Feature.ProfileTab.ProfileInformation -> {
            ProfileInformation(state, listener, setRightItemBeforeAction)
        }
        Feature.ProfileTab.Availability -> {
            availabilityState?.let {
                CurrentUserAvailabilityView(state = availabilityState) {
                    listener(Msg.AvailabilityMsg(it))
                }
            }
        }
    }
    if (state.requestConsultationState != null) {
        RequestConsultationBottomSheet(
            state = state.requestConsultationState!!,
            listener = {
                listener(Msg.RequestConsultationMsg(it))
            }
        )
    }
}

@Composable
private fun ProfileInformation(
    state: State,
    listener: (Msg) -> Unit,
    setRightItemBeforeAction: ((() -> Unit)?) -> Unit,
) {
    var modalContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    Box(Modifier.fillMaxSize()) {
        Content(
            state = state,
            listener = listener,
            setRightItemBeforeAction = setRightItemBeforeAction,
            showModalContent = {
                modalContent = it
            },
            modifier = Modifier.visibility(visible = modalContent == null)
        )
        if (modalContent != null) {
            BackHandler {
                modalContent = null
            }
            modalContent?.invoke()
        }
    }
}

@Composable
private fun Content(
    state: State,
    listener: (Msg) -> Unit,
    modifier: Modifier = Modifier,
    showModalContent: ((@Composable () -> Unit)?) -> Unit,
    setRightItemBeforeAction: ((() -> Unit)?) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        val paddingModifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        Box {
            val density = LocalDensity.current
            val bottomIme = with(density) { LocalWindowInsets.current.ime.bottom.toDp() }
            val (editingIndex, updateEditingIndex) = remember { mutableStateOf<Int?>(null) }
            var bottomOffsetInRoot by remember { mutableStateOf(0.dp) }
            val bottomOffset = remember(bottomIme, bottomOffsetInRoot) {
                (bottomIme - bottomOffsetInRoot).coerceAtLeast(0.dp)
            }
            val lazyState = rememberLazyListState()
            if (editingIndex != null) {
                LaunchedEffect(editingIndex, bottomOffset) {
                    lazyState.animateScrollToItem(editingIndex)
                }
            }
            LazyColumn(
                state = lazyState,
                contentPadding = PaddingValues(bottom = bottomOffset),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned {
                        bottomOffsetInRoot = with(density) {
                            (it.findRoot().size.height - it.boundsInRoot().bottom)
                                .toDp()
                        }
                    }
            ) {
                state.groups.forEachIndexed { groupIndex, group ->
                    when (group) {
                        is UIGroup.Preview -> {
                            items(group.fields) { field ->
                                PreviewField(field, paddingModifier, listener)
                            }
                        }
                        is UIGroup.Editing -> {
                            itemsIndexed(group.fields) { fieldIndex, field ->
                                EditingField(
                                    field,
                                    listener,
                                    onTextInputEditingHandler = { action ->
                                        setRightItemBeforeAction(action)
                                        updateEditingIndex(
                                            action?.let { _ ->
                                                fieldIndex + state
                                                    .groups
                                                    .subList(0, groupIndex)
                                                    .sumOf {
                                                        1 + when (it) {
                                                            is UIGroup.Preview -> {
                                                                it.fields.count()
                                                            }
                                                            is UIGroup.Editing -> {
                                                                it.fields.count()
                                                            }
                                                            is UIGroup.Header -> {
                                                                1
                                                            }
                                                        }
                                                    }
                                            }
                                        )
                                    },
                                    showModalContent = showModalContent,
                                    modifier = paddingModifier,
                                )
                            }
                        }
                        is UIGroup.Header -> {
                            item {
                                if (state.isCurrent) {
                                    CurrentUserHeader(
                                        modifier = paddingModifier,
                                        header = group,
                                        listener = listener,
                                    )
                                } else {
                                    OtherUserHeader(
                                        modifier = paddingModifier,
                                        header = group,
                                        listener = listener,
                                        editRating = {
                                            showModalContent {
                                                RatingScreen(
                                                    user = state.user!!,
                                                    maxCharacters = state.maxRatingCharacters,
                                                    rate = {
                                                        listener(Msg.Rate(it))
                                                    }
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Divider()
                    }
                }
            }
            if (state.editingStatus == State.EditingStatus.Uploading) {
                InactiveOverlay(showActivityIndicator = false)
            }
        }
    }
}

@Composable
private fun CurrentUserHeader(
    header: UIGroup.Header,
    listener: (Msg) -> Unit,
    modifier: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        header.image?.let {
            ProfileImage(
                it,
                modifier = Modifier
                    .size(100.dp)
                    .padding(end = 10.dp)
            )
        } ?: Spacer(modifier = Modifier.weight(1f))
        Column(
            verticalArrangement = Arrangement.Center,
        ) {
            val name = header.name
            if (name != null) {
                Text(name)
            } else {
                Text(
                    header.initiateImageUpdateText,
                    color = Color.LightBlue.toColor(),
                    modifier = Modifier
                        .clickable {
                            listener(Msg.InitiateImageUpdate)
                        }
                )
            }
            header.accountType?.let { accountType ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(id = accountType.drawable),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.LightBlue.toColor()),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(accountType.name, color = Color.LightBlue.toColor())
                }
            }
            header.completeness?.let { completeness ->
                Text(
                    "Profile $completeness% complete",
                    color = Color.LightGray.toColor(),
                    modifier = Modifier
                        .alpha(if (completeness < 100) 1f else 0f)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun OtherUserHeader(
    header: UIGroup.Header,
    listener: (Msg) -> Unit,
    editRating: () -> Unit,
    modifier: Modifier,
) {
    ProfileImage(
        header.image,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        squareCircleShaped = false
    )
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            header.accountType?.let { Text(it.toString()) }
            Spacer(modifier = Modifier.weight(1f))
            listOfNotNull(
                header.doximityLink?.let { it to R.drawable.ic_doximity },
                header.twitterLink?.let { it to R.drawable.ic_twitter },
            ).forEach {
                Control(onClick = {
                    listener(Msg.OpenUrl(it.first))
                }) {
                    Image(
                        painterResource(id = it.second),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.LightBlue.toColor()),
                    )
                }
            }
            ToggleFavoriteButton(favorite = header.favorite, toggle = {
                listener(Msg.ToggleFavorite)
            })
            Control(onClick = {
                listener.invoke(Msg.Message)
            }) {
                Icon(
                    Icons.Rounded.ChatBubble,
                    contentDescription = null,
                    tint = Color.Green.toColor(),
                )
            }
            Control(onClick = {
                listener.invoke(Msg.Call)
            }) {
                Icon(
                    Icons.Rounded.Call,
                    contentDescription = null,
                    tint = Color.Green.toColor(),
                )
            }
        }
        header.nameWithCredentials?.let { Text(it, style = MaterialTheme.typography.h4) }
        Control(onClick = {
            listener.invoke(Msg.Call)
        }) {
            Icon(
                Icons.Rounded.Call,
                contentDescription = null,
                tint = Color.Green.toColor(),
            )
        }
        RatingInfoView(header.ratingInfo, viewAll = editRating)
    }
}

private val User.Type.drawable
    get() = when (this) {
        User.Type.Doctor,
        User.Type.PendingExpert,
        User.Type.DeclinedExpert,
        -> R.drawable.ic_profile_doctor
        User.Type.Expert -> R.drawable.ic_profile_expert
    }