package com.well.androidApp.ui.composableScreens.myProfile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πCustomViews.InactiveOverlay
import com.well.androidApp.ui.composableScreens.πCustomViews.ModeledNavigationBar
import com.well.androidApp.ui.composableScreens.πCustomViews.UserProfileImage
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Color
import com.well.serverModels.User
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.State
import com.well.sharedMobile.puerh.myProfile.UIGroup
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature
import dev.chrisbanes.accompanist.insets.navigationBarsPadding

@Composable
fun MyProfileScreen(
    state: State,
    listener: (Msg) -> Unit,
) {
    val modalContent = remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

    if (modalContent.value == null) {
        Content(state, listener, showModalContent = {
            modalContent.value = it
        })
    } else {
        Box(Modifier.fillMaxSize()) {
            modalContent.value?.invoke()
        }
    }
}

@Composable
private fun Content(
    state: State,
    listener: (Msg) -> Unit,
    showModalContent: ((@Composable () -> Unit)?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        var currentEditingFieldHandler by remember { mutableStateOf<(() -> Unit)?>(null) }
        state.navigationBarModel?.let {
            ModeledNavigationBar(
                it,
                listener,
                rightItemBeforeAction = currentEditingFieldHandler,
                modifier = Modifier.fillMaxWidth()
            )
        }
        val paddingModifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        Box {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                state.groups.forEach { group ->
                    when (group) {
                        is UIGroup.Preview -> {
                            items(group.fields) { field ->
                                PreviewField(field, paddingModifier)
                            }
                        }
                        is UIGroup.Editing -> {
                            items(group.fields) { field ->
                                EditingField(
                                    field,
                                    listener,
                                    onTextInputEditingHandler = { finishEditingHandler ->
                                        currentEditingFieldHandler = finishEditingHandler
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
                                        group,
                                        listener,
                                        paddingModifier
                                    )
                                } else {
                                    OtherUserHeader(group, listener, paddingModifier)
                                }
                            }
                        }
                    }
                    item { Divider() }
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
            UserProfileImage(
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
    modifier: Modifier,
) {
    UserProfileImage(
        header.image,
        squareCircleShaped = false,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f)
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
            Control(onClick = {
                listener.invoke(Msg.Call)
            }) {
                Image(
                    painterResource(R.drawable.ic_baseline_call_24),
                    colorFilter = ColorFilter.tint(Color.Green.toColor())
                )
            }
        }
        header.nameWithCredentials?.let { Text(it, style = MaterialTheme.typography.h4) }
    }
}

private val User.Type.drawable
    get() = when (this) {
        User.Type.Doctor -> R.drawable.ic_profile_doctor
        User.Type.Expert -> R.drawable.ic_profile_expert
    }