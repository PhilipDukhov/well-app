package com.well.androidApp.ui.composableScreens.call.screenSharing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.Msg
import com.well.sharedMobile.puerh.call.imageSharing.ImageSharingFeature.State
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun ImageSharingScreen(
    state: State,
    listener: (Msg) -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
) {
    onActive {
        listener(Msg.UpdateLocalViewSize())
    }
    state.image?.let {
        CoilImage(
            it.bitmap,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}