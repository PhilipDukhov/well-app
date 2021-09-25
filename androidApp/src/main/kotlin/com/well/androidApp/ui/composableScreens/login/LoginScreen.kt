package com.well.androidApp.ui.composableScreens.login

import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.Control
import com.well.androidApp.ui.composableScreens.πCustomViews.GradientView
import com.well.androidApp.ui.composableScreens.πCustomViews.InactiveOverlay
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.sharedMobile.puerh.login.LoginFeature.Msg
import com.well.sharedMobile.puerh.login.LoginFeature.State
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.utils.Gradient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun LoginScreen(
    state: State,
    listener: (Msg) -> Unit,
) = Box(
    contentAlignment = Alignment.BottomCenter,
    modifier = Modifier.fillMaxSize()
) {
    Image(
        painterResource(R.drawable.login_background),
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.matchParentSize()
    )
    GradientView(
        gradient = Gradient.Login,
        modifier = Modifier.matchParentSize()
    )
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(bottom = 20.dp)
    ) {
        SocialNetwork.values().forEach { socialNetwork ->
            Control(
                vectorResourceId = socialNetwork.drawable,
                onClick = {
                    listener(Msg.OnSocialNetworkSelected(socialNetwork))
                },
            )
        }
    }
    if (state.processing) {
        InactiveOverlay()
    }
}

private val SocialNetwork.drawable: Int
    get() = when (this) {
        SocialNetwork.Facebook -> R.drawable.ic_auth_facebook
        SocialNetwork.Google -> R.drawable.ic_auth_google
        SocialNetwork.Twitter -> R.drawable.ic_auth_twitter
        SocialNetwork.Apple -> R.drawable.ic_auth_apple
    }
