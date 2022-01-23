package com.well.modules.androidUi.ext

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection


@Composable
operator fun PaddingValues.plus(second: PaddingValues) =
    PaddingValues(
        start = start() + second.start(),
        end = end() + second.end(),
        top = top() + second.top(),
        bottom = bottom() + second.bottom(),
    )

@Composable
operator fun PaddingValues.minus(second: PaddingValues) =
    PaddingValues(
        start = start() - second.start(),
        end = end() - second.end(),
        top = top() - second.top(),
        bottom = bottom() - second.bottom(),
    )

@Composable
fun PaddingValues.start() = calculateStartPadding(LocalLayoutDirection.current)

@Composable
fun PaddingValues.end() = calculateEndPadding(LocalLayoutDirection.current)

@Composable
fun PaddingValues.top() = calculateTopPadding()

@Composable
fun PaddingValues.bottom() = calculateBottomPadding()
