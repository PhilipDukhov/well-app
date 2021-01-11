package com.well.androidApp.ui.composableScreens.call.screenSharing

import android.content.res.Resources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.onActive
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.well.serverModels.Size
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
        Resources.getSystem()
            .displayMetrics
            .apply {
                listener(Msg.UpdateLocalViewSize(Size(widthPixels, heightPixels)))
        }
    }
    state.image?.let {
        CoilImage(
            it.data,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}