package org.permanent.permanent.ui.storage.compose.gifting.emailInput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.models.EmailChip

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmailChipTextField(
    errorText: MutableState<String>,
    emails: MutableList<EmailChip>,
    isFocused: MutableState<Boolean>,
    showTextField: MutableState<Boolean>,
    onAddEmailChip: (EmailChip) -> Unit,
    onRemoveEmailChip: (EmailChip) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val colorPrimary = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    LaunchedEffect(true) {
        focusRequester.requestFocus()

        snapshotFlow {
            isFocused.value
        }.drop(1)
            .onEach {
                if (!it) {
                    val email = textFieldValueState.text
                    if (Validator.isValidEmail(null, email = email, null, null)) {
                        onAddEmailChip(EmailChip(text = email))
                        textFieldValueState = TextFieldValue(text = "")
                        showTextField.value = false
                        errorText.value = ""
                    } else if (textFieldValueState.text.isEmpty()) {
                        showTextField.value = false
                        errorText.value = ""
                    } else {
                        errorText.value = email
                    }
                }
            }.launchIn(this)
    }

    Row(
        modifier = Modifier.defaultMinSize(minHeight = 33.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(value = textFieldValueState,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = regularFont,
                color = colorPrimary
            ),
            onValueChange = {
                if (it.text.lastOrNull() == ' ' || it.text.lastOrNull() == '\n' || it.text.lastOrNull() == ',') {
                    val email = it.text.substring(startIndex = 0, endIndex = it.text.count() - 1)
                    if (Validator.isValidEmail(null, email = email, null, null)) {
                        textFieldValueState = TextFieldValue(text = "")
                        onAddEmailChip(EmailChip(text = email))
                        errorText.value = ""
                    } else {
                        textFieldValueState = TextFieldValue(
                            text = email,
                            selection = TextRange(email.length))
                        errorText.value = email
                    }
                } else if (emails.isNotEmpty() && textFieldValueState.text.isEmpty() && it.text.isEmpty()) {
                    val last = emails[emails.count() - 1]
                    onRemoveEmailChip(last)
                    textFieldValueState = TextFieldValue(
                        text = last.text,
                        selection = TextRange(last.text.length))
                    errorText.value = ""
                } else {
                    textFieldValueState = it
                }
            },
            modifier = Modifier
                .background(Color.Transparent)
                .onKeyEvent {
                    if (it.key == Key.Backspace && textFieldValueState.text.isEmpty() && emails.isNotEmpty()) {
                        val last = emails[emails.count() - 1]
                        onRemoveEmailChip(last)
                        textFieldValueState = TextFieldValue(
                            text = last.text,
                            selection = TextRange(last.text.length)
                        )
                        errorText.value = ""
                    }
                    true
                }
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused.value = it.isFocused
                })
    }
}