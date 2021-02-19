package com.well.androidApp.ui.composableScreens.myProfile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.BackPressHandler
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.serverModels.Color
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.myProfile.UIEditingField
import com.well.sharedMobile.puerh.myProfile.UIEditingField.Content

@Composable
fun <C> EditingField(
    field: UIEditingField<C>,
    listener: (Msg) -> Unit,
    onTextInputEditingHandler: ((() -> Unit)?) -> Unit,
    showModalContent: ((@Composable () -> Unit)?) -> Unit,
    modifier: Modifier,
) where C : Content {
    val content = field.content
    val fieldName = content.toString()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        EditingTextField(
            text = fieldName,
            title = field.placeholder,
            onClick = if (content is Content.List) {
                {
                    showModalContent {
                        @Suppress("UNCHECKED_CAST")
                        ModalContent(
                            field as UIEditingField<Content.List>,
                            listener,
                            showModalContent,
                        )
                    }
                }
            } else null,
            leadingIcon = {
                content.icon?.let {
                    Image(
                        painterResource(it.drawable),
                        colorFilter = ColorFilter.tint(Color.LightBlue.toColor())
                    )
                }
            },
            onTextInputEditingHandler = onTextInputEditingHandler,
            onFinishEditing = { newText ->
                textContentUpdated(newText, field, listener)
            }
        )
    }
}

@Composable
private fun ModalContent(
    field: UIEditingField<Content.List>,
    listener: (Msg) -> Unit,
    showModalContent: ((@Composable () -> Unit)?) -> Unit,
) {
    SelectionScreen(
        title = field.placeholder,
        selection = field.content.selection,
        variants = field.content.list,
        multipleSelection = field.content.multipleSelectionAvailable,
        onSelectionChanged = {
            @Suppress("UNCHECKED_CAST")
            listener(field.updateMsg(field.content.copy(selection = it)))
            showModalContent(null)
        },
        onCancel = {
            showModalContent(null)
        },
    )
}

@Composable
private fun EditingTextField(
    text: String,
    title: String,
    onClick: (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    onTextInputEditingHandler: ((() -> Unit)?) -> Unit,
    onFinishEditing: (String) -> Unit,
) {
    var focusedState by remember { mutableStateOf(false) }
    // Grab a reference to the keyboard controller whenever text input starts
    var keyboardController by remember { mutableStateOf<SoftwareKeyboardController?>(null) }

    // Show or hide the keyboard
    DisposableEffect(
        keyboardController,
        focusedState
    ) { // Guard side-effects against failed commits
        keyboardController?.let {
            if (focusedState) it.showSoftwareKeyboard() else it.hideSoftwareKeyboard()
        }
        onDispose { /* no-op */ }
    }
    var textState by remember { mutableStateOf(text) }
    var focusState by remember { mutableStateOf(FocusState.Inactive) }
    val dismissKeyboard = { focusedState = false }
    val focusManager = LocalFocusManager.current
    val finishEditing = {
        focusedState = false
        onFinishEditing(textState)
        focusManager.clearFocus()
        dismissKeyboard()
    }

    // Intercept back navigation if there's a InputSelector visible
    if (focusedState) {
        BackPressHandler(onBackPressed = dismissKeyboard)
    }
    OutlinedTextField(
        readOnly = onClick != null,
        inactiveColor = Color.Black.toColor(),
        activeColor = Color.Green.toColor(),
        value = textState,
        onValueChange = {
            textState = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (focusState == it) return@onFocusChanged
                if (onClick != null) {
                    if (it == FocusState.Active) {
                        onClick()
                        focusedState = false
                    }
                } else {
                    when (it) {
                        FocusState.Active -> {
                            onTextInputEditingHandler(finishEditing)
                        }
                        FocusState.Inactive -> {
                            onTextInputEditingHandler(null)
                            finishEditing()
                        }
                        else -> Unit
                    }
                }
                focusState = it
            },
        textStyle = MaterialTheme.typography.body2,
        label = {
            Providers(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body2
                )
            }
        },
        leadingIcon = leadingIcon,
        onTextInputStarted = {
            keyboardController = it
            focusedState = true
        },
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { finishEditing() }
        ),
    )
}

@Suppress("UNCHECKED_CAST")
private fun <C> textContentUpdated(
    text: String,
    field: UIEditingField<C>,
    listener: (Msg) -> Unit,
) where C : Content {
    val content = field.content as Content
    if (text == content.toString()) return
    listener(
        field.updateMsg(
            when (content) {
                is Content.Text -> content.copy(text = text)
                is Content.Email -> content.copy(email = text)
                is Content.List ->
                    throw IllegalStateException("textContentUpdated shouldn't be called for a list")
            } as C
        )
    )
}

private val Content.Icon.drawable
    get() = when (this) {
        Content.Icon.Location -> {
            R.drawable.ic_outline_location_on_24
        }
    }
