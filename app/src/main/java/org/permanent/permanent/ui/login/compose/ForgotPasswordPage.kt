@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.login.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.AnimatedSnackbar
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.viewmodels.AuthenticationViewModel

@Composable
fun ForgotPasswordPage(
    viewModel: AuthenticationViewModel, pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    var emailValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    val keyboardState by keyboardAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.forgot_your_password_title),
                fontSize = 32.sp,
                lineHeight = 48.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.forgot_your_password_text),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (keyboardState == Keyboard.Closed) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.29f), RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = emailValueState,
                        onValueChange = { value -> emailValueState = value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .weight(1.0f),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.email_address).uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 16.sp,
                                fontFamily = regularFont
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontFamily = regularFont,
                            fontWeight = FontWeight(600),
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = colorResource(id = R.color.whiteUltraTransparent),
                            unfocusedContainerColor = colorResource(id = R.color.whiteUltraTransparent),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = colorResource(id = R.color.blue400),
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.LIGHT,
                    text = stringResource(id = R.string.send_request)
                ) {
                    keyboardController?.hide()
                    viewModel.clearSnackbar()
                    viewModel.forgotPassword(emailValueState.text.trim())
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        color = colorResource(id = R.color.colorPrimary200),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(id = R.string.or).uppercase(),
                        color = colorResource(id = R.color.colorPrimary200),
                        fontSize = 10.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    HorizontalDivider(
                        color = colorResource(id = R.color.colorPrimary200),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.TRANSPARENT,
                    text = stringResource(id = R.string.back_to_sign_in),
                    icon = painterResource(id = R.drawable.ic_back_white),
                    iconAlignment = ButtonIconAlignment.START,
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(AuthPage.SIGN_IN.value)
                    }
                }
            }
        }

        AnimatedSnackbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isForError = snackbarType == SnackbarType.ERROR,
            message = snackbarMessage,
            buttonText = stringResource(id = R.string.ok),
            onButtonClick = {
                viewModel.clearSnackbar()
            })
    }
}