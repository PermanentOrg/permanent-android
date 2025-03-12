package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.permanent.permanent.R

@Composable
fun DigitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    previousFocusRequester: FocusRequester?,  // Add previous focus requester for backspace handling
    nextFocusRequester: FocusRequester?,      // Keep next focus requester to move forward
    modifier: Modifier = Modifier,
    colors: DigitTextFieldColor = DigitTextFieldColor.TRANSPARENT,
) {
    val coroutineScope = rememberCoroutineScope()

    TextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numeric input and overwrite current value
            if (newValue.length == 1 && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
                nextFocusRequester?.requestFocus()
            } else if (newValue.isEmpty()) {
                onValueChange("") // Clear value

                // Move focus to the previous field inside a coroutine
                previousFocusRequester?.let { prevFocus ->
                    coroutineScope.launch {
                        delay(10) // Small delay to allow Compose to process the change
                        prevFocus.requestFocus()
                    }
                }
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp) {
                    // Handle backspace: clear the current field and move to the previous one
                    onValueChange("") // Clear current field
                    // Move focus to the previous field inside a coroutine
                    previousFocusRequester?.let { prevFocus ->
                        coroutineScope.launch {
                            delay(10) // Allow UI updates before requesting focus
                            prevFocus.requestFocus()
                        }
                    }
                    true // Consume the event
                } else {
                    false // Let other events be handled normally
                }
            },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocusRequester == null) ImeAction.Done else ImeAction.Next
        ),
        textStyle = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight(400),
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = if (colors == DigitTextFieldColor.TRANSPARENT) Color.White else colorResource(id = R.color.blue900),
            unfocusedTextColor = if (colors == DigitTextFieldColor.TRANSPARENT) Color.White else colorResource(id = R.color.blue900),
            focusedContainerColor = colorResource(id = if (colors == DigitTextFieldColor.TRANSPARENT) R.color.whiteUltraTransparent else R.color.white),
            unfocusedContainerColor = colorResource(id = if (colors == DigitTextFieldColor.TRANSPARENT) R.color.whiteUltraTransparent else R.color.white),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = colorResource(id = if (colors == DigitTextFieldColor.TRANSPARENT) R.color.blue400 else R.color.blue200),
        ),
        maxLines = 1
    )
}

enum class DigitTextFieldColor {
    TRANSPARENT,
    LIGHT
}
