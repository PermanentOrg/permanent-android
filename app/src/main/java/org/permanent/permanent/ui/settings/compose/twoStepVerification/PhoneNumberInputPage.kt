package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton

@Composable
fun PhoneNumberInputPage(
    onBack: () -> Unit, onDismiss: () -> Unit, onSendCodeOn: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Custom Back Button
            IconButton(
                onClick = onBack, modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back_rounded_white),
                    contentDescription = "Back",
                    tint = colorResource(R.color.blue900),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Centered Title
            Text(
                text = stringResource(R.string.add_text_verification_method),
                color = colorResource(R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )

            // Custom Close Button
            IconButton(
                onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close_middle_grey),
                    contentDescription = "Close",
                    tint = colorResource(R.color.blue900),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider below the header
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(), color = colorResource(R.color.blue50)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            // Instruction Text
            Text(
                text = buildAnnotatedString {
                    val fullText = stringResource(R.string.add_text_verification_description)
                    val boldText = "North American"
                    val startIndex = fullText.indexOf(boldText)
                    val endIndex = startIndex + boldText.length

                    append(fullText)

                    addStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold),
                        start = startIndex,
                        end = endIndex
                    )
                },
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue)
            )

            Spacer(modifier = Modifier.height(32.dp))

            var phoneNrState by remember { mutableStateOf(TextFieldValue("+1 ")) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp, colorResource(R.color.blue100), RoundedCornerShape(12.dp)
                    ), verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = phoneNrState,
                    onValueChange = { newValue ->
                        if (newValue.text.length < 3) {
                            phoneNrState = TextFieldValue("+1 ", selection = TextRange(3))
                            return@TextField
                        }

                        val (newFormattedText, newCursorPosition) = formatPhoneNumber(newValue.text, phoneNrState.text)

                        phoneNrState = TextFieldValue(
                            text = newFormattedText,
                            selection = TextRange(newCursorPosition)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .weight(1.0f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.example_phone_number),
                            color = colorResource(R.color.colorPrimary200),
                            fontSize = 14.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily(Font(R.font.usual_regular))
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular))
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = colorResource(R.color.blue900),
                        unfocusedTextColor = colorResource(R.color.blue900),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = colorResource(id = R.color.blue400)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Regex pattern to validate US phone number format: +1 (XXX) XXX - XXXX
            val phoneNumberRegex = "^\\+1 \\([0-9]{3}\\) [0-9]{3} - [0-9]{4}$".toRegex()

            // Confirm Button
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.send_code),
                icon = null,
                enabled = phoneNumberRegex.matches(phoneNrState.text),
                disabledColor = colorResource(R.color.colorPrimary200)
            ) {
                onSendCodeOn(phoneNrState.text)
            }
        }
    }
}

fun formatPhoneNumber(input: String, previous: String): Pair<String, Int> {
    if (input.length < 3) return "+1 " to 3 // Ensure "+1 " stays

    val rawDigits = input.filter { it.isDigit() } // Keep only digits
    if (rawDigits.length < 2) return "+1 " to 3 // Keep "+1 " when all digits are deleted

    val maxDigits = 11 // +1 plus 10-digit phone number
    val trimmedDigits = rawDigits.take(maxDigits)

    val formatted = StringBuilder("+1 ")
    var cursorPosition = input.length

    for (i in 1 until trimmedDigits.length) {
        when (i) {
            1 -> formatted.append("(")
            4 -> formatted.append(") ")
            7 -> formatted.append(" - ")
        }
        formatted.append(trimmedDigits[i])
    }

    // Adjust cursor to prevent jumps
    val isDeleting = input.length < previous.length
    if (isDeleting) {
        if (previous.endsWith(" - ") || previous.endsWith(") ") || previous.endsWith("(")) {
            cursorPosition -= 2
        }
    } else {
        cursorPosition = formatted.length
    }

    return formatted.toString() to maxOf(3, cursorPosition) // Ensure cursor is always after "+1 "
}
