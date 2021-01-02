package com.well.androidApp.ui.composableScreens.zCustomViews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.well.serverModels.User
import dev.chrisbanes.accompanist.coil.CoilImage

@Suppress("NAME_SHADOWING")
@Composable
fun UserProfileImage(
    user: User,
    modifier: Modifier,
) = modifier
    .clip(CircleShape)
    .aspectRatio(1F)
    .let { modifier ->
        user.profileImageUrl?.let {
            CoilImage(
                data = it,
                loading = {
                    Box {
                        CircularProgressIndicator(
                            Modifier
                                .align(Alignment.Center)
                                .fillMaxSize(0.7F)
                        )
                    }
                },
                modifier = modifier
            )
        } ?: Box(
            modifier = modifier
                .background(Color.Red)
        )
    }
