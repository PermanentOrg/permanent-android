package org.permanent.permanent.ui.settings.compose.twoStepVerification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import org.permanent.permanent.ui.composeComponents.DigitTextField
import org.permanent.permanent.ui.composeComponents.DigitTextFieldColor
import org.permanent.permanent.ui.composeComponents.TimerButton
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun PhoneNumberInputPage(
    viewModel: LoginAndSecurityViewModel, onBack: () -> Unit, onDismiss: () -> Unit
) {
    val isTablet = viewModel.isTablet()
    var isCodeSent by remember { mutableStateOf(false) }
    val codeValues by viewModel.codeValues.collectAsState()
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()
    var phoneNrState by remember { mutableStateOf(TextFieldValue("+1 ")) }
    // Regex pattern to validate US phone number format: +1 (XXX) XXX - XXXX
    val phoneNumberRegex = "^\\+1 \\([0-9]{3}\\) [0-9]{3} - [0-9]{4}$".toRegex()

    // Detect keyboard visibility
    val isKeyboardVisible = rememberKeyboardVisibility()
    LaunchedEffect(isKeyboardVisible) {
        if (isKeyboardVisible) {
            viewModel.clearSnackbar()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Custom Back Button
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    if (isCodeSent) {
                        isCodeSent = false
                        viewModel.updateCodeValues(List(4) { "" })
                        viewModel.clearSnackbar()
                    } else {
                        onBack()
                    }
                }, modifier = Modifier.align(Alignment.TopStart)
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

        // Main content (scrollable)
        Column(
            modifier = Modifier
                .background(colorResource(if (isCodeSent) R.color.blue25 else R.color.white))
                .weight(1f) // Allows the content to take remaining space
                .verticalScroll(scrollState) // Enables scrolling when needed
                .imePadding() // Moves up when keyboard is visible
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(R.color.white))
                    .padding(32.dp)
            ) {
                if (!(isKeyboardVisible && isTablet && isCodeSent)) {
                    // Instruction Text
                    Text(
                        text = buildAnnotatedString {
                            val fullText =
                                stringResource(R.string.add_text_verification_description)
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

                                val (newFormattedText, _) = formatPhoneNumber(
                                    newValue.text,
                                    phoneNrState.text
                                )

                                phoneNrState = TextFieldValue(
                                    text = newFormattedText,
                                    selection = TextRange(newFormattedText.length) // Move cursor to end
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .weight(1.0f),
                            singleLine = true,
                            enabled = !isCodeSent,
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
                                disabledTextColor = colorResource(R.color.blueGreyLight),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = colorResource(id = R.color.blue400)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (isCodeSent) {
                    // Resend code Button
                    TimerButton(
                        text = stringResource(id = R.string.resend_code)
                    ) {
                        keyboardController?.hide()
                        viewModel.sendEnableCode(VerificationMethod.SMS,
                            phoneNrState.text,
                            successCallback = {
                                isCodeSent = true
                            })
                    }
                } else {
                    // Send code Button
                    CenteredTextAndIconButton(
                        buttonColor = ButtonColor.DARK,
                        text = stringResource(id = R.string.send_code),
                        icon = null,
                        enabled = phoneNumberRegex.matches(phoneNrState.text),
                        disabledColor = colorResource(R.color.colorPrimary200)
                    ) {
                        keyboardController?.hide()
                        viewModel.sendEnableCode(
                            VerificationMethod.SMS,
                            phoneNrState.text,
                            successCallback = {
                                isCodeSent = true
                            })
                    }
                }
            }

            if (isCodeSent) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.enter_code_received),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        codeValues.forEachIndexed { index, codeValue ->
                            DigitTextField(
                                value = codeValue,
                                onValueChange = { newValue ->
                                    val updatedValues =
                                        codeValues.toMutableList().also { it[index] = newValue }
                                    viewModel.updateCodeValues(updatedValues)
                                },
                                focusRequester = focusRequesters[index],
                                previousFocusRequester = if (index > 0) focusRequesters[index - 1] else null,
                                nextFocusRequester = if (index < 3) focusRequesters[index + 1] else null,
                                modifier = Modifier
                                    .height(56.dp)
                                    .weight(1f)
                                    .border(
                                        1.dp,
                                        colorResource(id = R.color.blue100),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = DigitTextFieldColor.LIGHT
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Enable Button
                    CenteredTextAndIconButton(
                        buttonColor = ButtonColor.DARK,
                        text = stringResource(id = R.string.enable),
                        icon = null,
                        enabled = codeValues.all { it.isNotEmpty() },
                        disabledColor = colorResource(R.color.colorPrimary200)
                    ) {
                        val code = codeValues.joinToString("")
                        viewModel.enableTwoFactor(VerificationMethod.SMS,
                            phoneNrState.text,
                            code,
                            successCallback = {
                                keyboardController?.hide()
                                onDismiss()
                            })
                    }
                }
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
