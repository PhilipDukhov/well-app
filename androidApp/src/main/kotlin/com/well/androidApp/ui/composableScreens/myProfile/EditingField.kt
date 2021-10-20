package com.well.androidApp.ui.composableScreens.myProfile

import com.well.androidApp.ui.composableScreens.πCustomViews.ControlItem
import com.well.androidApp.ui.composableScreens.πCustomViews.NavigationBar
import com.well.androidApp.ui.composableScreens.πCustomViews.clearFocusOnKeyboardDismiss
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.πModels.UIEditingField
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction

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
    val selectionState = remember { mutableStateOf(field.content.selectionIndices) }
    val onFinish = { selection: Set<Int> ->
        listener(field.updateMsg(field.content.doCopy(selectionIndices = selection)))
        showModalContent(null)
    }
    Column(Modifier.fillMaxSize()) {
        NavigationBar(
            title = field.placeholder,
            leftItem = ControlItem(text = "Cancel") {
                showModalContent(null)
            },
            rightItem = if (field.content.multipleSelectionAvailable) ControlItem(text = "Done") {
                onFinish(selectionState.value)
            } else null,
        )
        SelectionScreen(
            selectionState = selectionState,
            variants = field.content.itemDescriptions,
            multipleSelection = field.content.multipleSelectionAvailable,
            onFinish = onFinish,
        )
    }
}

@Composable
private fun EditingTextField(
    text: String,
    title: String,
    onClick: (() -> Unit)?,
    onTextInputEditingHandler: ((() -> Unit)?) -> Unit,
    onFinishEditing: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    var textState by remember { mutableStateOf(text) }
    val focusManager = LocalFocusManager.current
    val finishEditing = {
        isFocused = false
        onFinishEditing(textState)
        focusManager.clearFocus()
    }

    // Intercept back navigation if there's a InputSelector visible
    if (isFocused) {
        BackHandler {
            isFocused = false
        }
    }
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
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { finishEditing() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clearFocusOnKeyboardDismiss()
            .onFocusChanged {
                if (isFocused == it.isFocused) return@onFocusChanged
                isFocused = it.isFocused
                println("onFocusChanged $text $isFocused")
                if (onClick != null) {
                    if (isFocused) {
                        onClick()
                    }
                } else {
                    if (isFocused) {
                        onTextInputEditingHandler(finishEditing)
                    } else {
                        onTextInputEditingHandler(null)
//                        finishEditing()
                    }
                }
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
