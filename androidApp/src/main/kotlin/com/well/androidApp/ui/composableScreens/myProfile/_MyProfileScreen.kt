package com.well.androidApp.ui.composableScreens.myProfile

import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.myProfile.availability.CurrentUserAvailabilityView
import com.well.androidApp.ui.customViews.Control
import com.well.androidApp.ui.customViews.InactiveOverlay
import com.well.androidApp.ui.customViews.ModeledNavigationBar
import com.well.androidApp.ui.customViews.ProfileImage
import com.well.androidApp.ui.customViews.RatingInfoView
import com.well.androidApp.ui.customViews.ToggleFavoriteButton
import com.well.androidApp.ui.customViews.controlMinSize
import com.well.androidApp.ui.ext.Image
import com.well.androidApp.ui.ext.heightPlusTopSystemBars
import com.well.androidApp.ui.ext.toColor
import com.well.androidApp.ui.ext.visibility
import com.well.modules.models.Color
import com.well.modules.models.User
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.State
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature as Feature
import com.well.sharedMobile.puerh.myProfile.UIGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsWithImePadding
import io.github.aakira.napier.Napier

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
    var selectedTab by rememberSaveable {
        mutableStateOf(
            availabilityState?.let {
                Feature.Tabs.ProfileInformation
            }
        )
    }
    state.navigationBarModel?.let {
        ModeledNavigationBar(
            it.copy(
                rightItem = when (selectedTab) {
                    null, Feature.Tabs.ProfileInformation -> it.rightItem
                    else -> null
                }
            ),
            listener,
            rightItemBeforeAction = rightItemBeforeAction,
            extraContent = {
                if (selectedTab != null && state.editingStatus == State.EditingStatus.Preview) {
                    Napier.d("selectedTab $selectedTab")
                    TabRow(
                        selectedTabIndex = Feature.Tabs.values().indexOf(selectedTab!!),
                        backgroundColor = Color.Transparent.toColor(),
                        contentColor = Color.White.toColor(),
                    ) {
                        Feature.Tabs.values().forEach { tab ->
                            Tab(
                                text = { Text(tab.title) },
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab }
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
        null, Feature.Tabs.ProfileInformation -> {
            ProfileInformation(state, listener, setRightItemBeforeAction)
        }
        Feature.Tabs.Availability -> {
            availabilityState?.let {
                CurrentUserAvailabilityView(state = availabilityState) {
                    listener(Msg.AvailabilityMsg(it))
                }
            }
        }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsWithImePadding()
            ) {
                state.groups.forEach { group ->
                    when (group) {
                        is UIGroup.Preview -> {
                            items(group.fields) { field ->
                                PreviewField(field, paddingModifier, listener)
                            }
                        }
                        is UIGroup.Editing -> {
                            items(group.fields) { field ->
                                EditingField(
                                    field,
                                    listener,
                                    onTextInputEditingHandler = setRightItemBeforeAction,
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
                    painterResource(R.drawable.ic_round_chat_bubble_24),
                    contentDescription = "",
                    tint = Color.Green.toColor(),
                )
            }
            Control(onClick = {
                listener.invoke(Msg.Call)
            }) {
                Icon(
                    painterResource(R.drawable.ic_baseline_call_24),
                    contentDescription = "",
                    tint = Color.Green.toColor(),
                )
            }
        }
        header.nameWithCredentials?.let { Text(it, style = MaterialTheme.typography.h4) }
        Control(onClick = {
            listener.invoke(Msg.Call)
        }) {
            Icon(
                painterResource(R.drawable.ic_baseline_call_24),
                contentDescription = "",
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