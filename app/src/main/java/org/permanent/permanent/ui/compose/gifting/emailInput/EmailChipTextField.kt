package org.permanent.permanent.ui.compose.gifting.emailInput

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.models.EmailChip

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmailChipTextField(text: MutableState<String>,
                       errorText: MutableState<String>,
                       emails: MutableList<EmailChip>) {

    var isFocused = remember { mutableStateOf(false) }

    val context = LocalContext.current
    val colorPrimary = Color(ContextCompat.getColor(context, R.color.colorPrimary))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Row(
        modifier = Modifier.defaultMinSize(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(value = text.value,
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontFamily = regularFont,
                color = colorPrimary
            ),
            onValueChange = {
                if(it.lastOrNull() == ' ' || it.lastOrNull() == '\n' || it.lastOrNull() == ',') {
                    val email = it.substring(startIndex = 0, endIndex = it.count() - 1)
                    if(Validator.isValidEmail(null, email = email, null, null)) {
                        text.value = ""
                        emails.add(index = emails.count() - 1, EmailChip(text = email))
                        errorText.value = ""
                    } else {
                        text.value = email
                        errorText.value = email
                    }
                } else if (emails.count() > 1 && text.value.isEmpty() && it.isEmpty()) {
                    val last = emails.removeAt(emails.count() - 2)
                    text.value = last.text
                    errorText.value = ""
                } else {
                    text.value = it
                }
            },
            modifier = Modifier
                .background(Color.Transparent)
                .onKeyEvent {
                    if (it.key == Key.Backspace && text.value.isEmpty() && emails.count() > 1) {
                        val last = emails.removeAt(emails.count() - 2)
                        text.value = last.text
                    }
                    true
                }
                .onFocusChanged {
                    if (!it.isFocused && isFocused.value) {
                        val email = text.value
                        if (Validator.isValidEmail(null, email = email, null, null)) {
                            emails.add(index = emails.count() - 1, EmailChip(text = email))
                            text.value = ""
                            emails.removeLast()
                        } else if (text.value.isEmpty()) {
                            emails.removeLast()
                        }
                    }
                    isFocused.value = it.isFocused
                })
    }
}