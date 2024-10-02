package org.permanent.permanent.ui.composeComponents

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import org.permanent.permanent.R

@Composable
fun DigitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    previousFocusRequester: FocusRequester?,  // Add previous focus requester for backspace handling
    nextFocusRequester: FocusRequester?,      // Keep next focus requester to move forward
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = { newValue ->
            // Only allow numeric input and overwrite current value
            if (newValue.length == 1 && newValue.all { it.isDigit() }) {
                onValueChange(newValue)

                // Move to the next text field if not null
                nextFocusRequester?.requestFocus()
            } else if (newValue.isEmpty()) {
                onValueChange("") // Handle deletion (clear value)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyUp) {
                    // Handle backspace: clear the current field and move to the previous one
                    onValueChange("") // Clear current field
                    previousFocusRequester?.requestFocus() // Move to the previous field
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
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = colorResource(id = R.color.whiteUltraTransparent),
            unfocusedContainerColor = colorResource(id = R.color.whiteUltraTransparent),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = colorResource(id = R.color.blue400),
        ),
        maxLines = 1
    )
}
