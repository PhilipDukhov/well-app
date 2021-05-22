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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.πCustomViews.BackPressHandler
import com.well.androidApp.ui.composableScreens.πExt.Image
import com.well.androidApp.ui.composableScreens.πExt.toColor
import com.well.modules.models.Color
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.πModels.UIEditingField

@Composable
fun <Content> EditingField(
    field: UIEditingField<Content, Msg>,
    listener: (Msg) -> Unit,
    onTextInputEditingHandler: ((() -> Unit)?) -> Unit,
    showModalContent: ((@Composable () -> Unit)?) -> Unit,
    modifier: Modifier,
) where Content : UIEditingField.Content {
    val content = field.content
    val fieldName = content.toString()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        EditingTextField(
            text = fieldName,
            title = field.placeholder,
            onClick = if (content is UIEditingField.Content.List<*>) {
                {
                    showModalContent {
                        @Suppress("UNCHECKED_CAST")
                        ModalContent(
                            field as UIEditingField<UIEditingField.Content.List<Any>, Msg>,
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
    field: UIEditingField<UIEditingField.Content.List<Any>, Msg>,
    listener: (Msg) -> Unit,
    showModalContent: ((@Composable () -> Unit)?) -> Unit,
) {
    SelectionScreen(
        title = field.placeholder,
        selection = field.content.selectionIndices,
        variants = field.content.itemDescriptions,
        multipleSelection = field.content.multipleSelectionAvailable,
        onSelectionChanged = {
            @Suppress("UNCHECKED_CAST")
            listener(field.updateMsg(field.content.doCopy(selectionIndices = it)))
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
    LocalSoftwareKeyboardController.current?.run {
        if (focusedState)
            show()
        else
            hide()
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
//    visualTransformation: VisualTransformation = VisualTransformation.None,
//    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
//    keyboardActions: KeyboardActions = KeyboardActions.Default,
//    singleLine: Boolean = false,
//    maxLines: Int = Int.MAX_VALUE,
//    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
//    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors()
    OutlinedTextField(
        value = textState,
        onValueChange = {
            textState = it
        },
        readOnly = onClick != null,
        textStyle = MaterialTheme.typography.body2,
        label = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.body2
                )
            }
        },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { finishEditing() }
        ),
        colors = TextFieldDefaults.textFieldColors(),
//        inactiveColor = Color.Black.toColor(),
//        activeColor = Color.Green.toColor(),
//        onTextInputStarted = {
//            keyboardController = it
//            focusedState = true
//        },
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
            }
    )
}

@Suppress("UNCHECKED_CAST")
private fun <Content> textContentUpdated(
    text: String,
    field: UIEditingField<Content, Msg>,
    listener: (Msg) -> Unit,
) where Content : UIEditingField.Content {
    val content = field.content
    if (text == content.toString()) return
    listener(
        field.updateMsg(
            when (content) {
                is UIEditingField.Content.Text -> content.copy(text = text)
                is UIEditingField.Content.Email -> content.copy(email = text)
                else -> throw IllegalStateException("textContentUpdated shouldn't be called for a $content")
            } as Content
        )
    )
}

private val UIEditingField.Content.Icon.drawable
    get() = when (this) {
        UIEditingField.Content.Icon.Location -> {
            R.drawable.ic_outline_location_on_24
        }
    }
