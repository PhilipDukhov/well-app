package com.well.androidApp.ui.composableScreens.πCustomViews

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import com.google.accompanist.insets.LocalWindowInsets
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.fixKeyboardFocusIssue(): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    if (isFocused) {
        // TODO: replace with single if when https://issuetracker.google.com/issues/193907134 fixed
        if (!LocalWindowInsets.current.ime.isVisible) {
            LocalFocusManager.current.clearFocus()
        }
    }
    onFocusChanged {
        if (it.isFocused && !isFocused) {
            scope.launch {
                // wait until keyboard presented on start editing
                delay(300)
                isFocused = it.isFocused
            }
        } else {
            isFocused = it.isFocused
        }
    }
}
