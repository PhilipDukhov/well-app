package com.well.androidApp.ui.composableScreens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AmbientContext
import androidx.viewbinding.BuildConfig
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.call.CallScreen
import com.well.androidApp.ui.composableScreens.call.screenSharing.ImageSharingScreen
import com.well.serverModels.Date
import com.well.serverModels.User
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.VideoViewContext
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature
import com.well.sharedMobile.utils.ImageContainer
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import java.net.URI

const val testing = BuildConfig.DEBUG && false

@Composable
fun TestComposeScreen() {
    CallTest()
}

@Composable
private fun CallTest() {
    val state = remember { mutableStateOf(CallFeature.testState(CallFeature.State.Status.Calling)) }
    CallScreen(
        state = state.value,
        listener = {
            state.value = state.value.testIncStatus()
        })
}

@Composable
private fun ImageSharingTest() {
    val state = remember {
        mutableStateOf<ImageSharingFeature.State?>(
            null
        )
    }
    if (state.value == null) {
        state.value = ImageSharingFeature.testState(
            ImageContainer(
                Uri(drawableId = R.drawable.ic_test_img),
                AmbientContext.current
            )
        )
    }
    ImageSharingScreen(state.value!!) {
        state.value = ImageSharingFeature.reducerMeasuring(it, state.value!!).first
    }
}

fun Uri(drawableId: Int) =
    Uri.parse("android.resource://com.well.androidApp/$drawableId")