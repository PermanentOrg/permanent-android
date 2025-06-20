@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package org.permanent.permanent.ui.login.compose

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.AnimatedSnackbar
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CustomTextButton
import org.permanent.permanent.ui.composeComponents.DigitTextField
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.ui.openLink
import org.permanent.permanent.viewmodels.AuthenticationViewModel

@Composable
fun CodeVerificationPage(
    viewModel: AuthenticationViewModel
) {
    val context = LocalContext.current
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardState by keyboardAsState()

    val codeValues by viewModel.codeValues.collectAsState()
    val focusRequesters = remember { List(4) { FocusRequester() } }

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
                text = stringResource(id = R.string.verify_your_identity_title),
                fontSize = 32.sp,
                lineHeight = 48.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.verify_your_identity_text),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (keyboardState == Keyboard.Closed) {
                Spacer(modifier = Modifier.height(64.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                codeValues.forEachIndexed { index, codeValue ->
                    DigitTextField(
                        value = codeValue,
                        onValueChange = { newValue ->
                            val updatedValues = codeValues.toMutableList().also { it[index] = newValue }
                            viewModel.updateCodeValues(updatedValues)
                        },
                        focusRequester = focusRequesters[index],
                        previousFocusRequester = if (index > 0) focusRequesters[index - 1] else null,
                        nextFocusRequester = if (index < 3) focusRequesters[index + 1] else null,
                        modifier = Modifier
                            .height(64.dp)
                            .width(70.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.29f), RoundedCornerShape(12.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CenteredTextAndIconButton(
                buttonColor = ButtonColor.LIGHT,
                text = stringResource(id = R.string.verify),
                icon = null
            ) {
                keyboardController?.hide()
                val code = codeValues.joinToString("")
                viewModel.verifyCode(code) {
                    focusRequesters[0].requestFocus() // Request focus to the first digit field after clearing the code
                    keyboardController?.hide()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (keyboardState == Keyboard.Closed) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    color = colorResource(id = R.color.colorPrimary200),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.didnt_receive_code).uppercase(),
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
                text = stringResource(id = R.string.resend_code),
                icon = null
            ) {
                keyboardController?.hide()
                viewModel.resendCode()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.or).uppercase(),
                    color = colorResource(id = R.color.colorPrimary200),
                    fontSize = 10.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf))
                )
            }

            CustomTextButton(text = stringResource(id = R.string.contact_support)) {
                context.openLink("https://permanent.zohodesk.com/portal/en/newticket")
            }
        }

        AnimatedSnackbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            isForError = snackbarType == SnackbarType.ERROR,
            message = snackbarMessage,
            buttonText = stringResource(id = R.string.ok),
            onButtonClick = {
                viewModel.clearSnackbar()
            },
        )
    }
}
