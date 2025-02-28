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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.Validator
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.DigitTextField
import org.permanent.permanent.ui.composeComponents.DigitTextFieldColor
import org.permanent.permanent.ui.composeComponents.TimerButton
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun EmailAddressInputPage(
    viewModel: LoginAndSecurityViewModel, onBack: () -> Unit, onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    val codeValues by viewModel.codeValues.collectAsState()
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
                text = stringResource(R.string.add_email_verification_method),
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
                // Instruction Text
                Text(
                    text = stringResource(R.string.add_email_verification_description),
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
                        value = email,
                        onValueChange = { value -> email = value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .weight(1.0f),
                        singleLine = true,
                        enabled = !isCodeSent, // Disable input when code is sent
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.example_email),
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

                if (isCodeSent) {
                    // Resend code Button
                    TimerButton(
                        text = stringResource(id = R.string.resend_code), startImmediately = true
                    ) {
                        keyboardController?.hide()
                        viewModel.sendEnableCode(VerificationMethod.EMAIL,
                            email,
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
                        enabled = Validator.isValidEmail(null, email, null, null),
                        disabledColor = colorResource(R.color.colorPrimary200)
                    ) {
                        keyboardController?.hide()
                        viewModel.sendEnableCode(VerificationMethod.EMAIL,
                            email,
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
                        text = stringResource(R.string.enter_code_description),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
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
                                    .height(64.dp)
                                    .width(70.dp)
                                    .border(
                                        1.dp,
                                        colorResource(id = R.color.blue100),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            viewModel.clearSnackbar()
                                        }
                                    },
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
                        viewModel.enableTwoFactor(VerificationMethod.EMAIL,
                            email,
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
