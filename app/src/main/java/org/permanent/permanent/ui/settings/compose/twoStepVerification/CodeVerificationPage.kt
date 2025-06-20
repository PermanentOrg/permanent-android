@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.settings.compose.twoStepVerification

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CustomTextButton
import org.permanent.permanent.ui.composeComponents.DigitTextField
import org.permanent.permanent.ui.composeComponents.DigitTextFieldColor
import org.permanent.permanent.ui.composeComponents.TimerButton
import org.permanent.permanent.ui.openLink
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

@Composable
fun CodeVerificationPage(
    viewModel: LoginAndSecurityViewModel,
    pagerState: PagerState,
    onMethodDisabled: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isCodeSent by remember { mutableStateOf(false) }
    val codeValues by viewModel.codeValues.collectAsState()
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Detect keyboard visibility
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    LaunchedEffect(imeVisible) {
        if (imeVisible) {
            viewModel.clearSnackbar()
        }
    }

    // Trigger request when this page is visible
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == TwoStepVerificationPage.CODE_VERIFICATION.value) {
            viewModel.sendTwoFADisableCode(successCallback = {
                isCodeSent = true
            })
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
            // Centered Title
            Text(
                text = stringResource(R.string.two_step_verification),
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
                .fillMaxWidth()
                .padding(32.dp)
                .background(colorResource(R.color.white))
                .weight(1f) // Allows the content to take remaining space
                .verticalScroll(scrollState) // Enables scrolling when needed
                .imePadding() // Moves up when keyboard is visible
        ) {
            Text(
                text = stringResource(R.string.enter_code_to_confirm),
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

            // Verify Button
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.verify),
                icon = null,
                enabled = codeValues.all { it.isNotEmpty() },
                disabledColor = colorResource(R.color.colorPrimary200)
            ) {
                val code = codeValues.joinToString("")
                viewModel.disableTwoFactor(code = code, successCallback = {
                    keyboardController?.hide()
                    if (viewModel.isChangeVerificationMethod.value) onMethodDisabled() else onDismiss()
                })
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    color = colorResource(id = R.color.blue100), modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(id = R.string.didnt_receive_code).uppercase(),
                    color = colorResource(id = R.color.blue600),
                    fontSize = 10.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf))
                )

                Spacer(modifier = Modifier.width(16.dp))

                HorizontalDivider(
                    color = colorResource(id = R.color.blue100), modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Resend code Button
            TimerButton(
                text = stringResource(id = R.string.resend_code)
            ) {
                viewModel.sendTwoFADisableCode(successCallback = {
                    isCodeSent = true
                })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.or).uppercase(),
                    color = colorResource(id = R.color.blue600),
                    fontSize = 10.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 1.6.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular))
                )
            }

            CustomTextButton(
                style = ButtonColor.TRANSPARENT,
                text = stringResource(id = R.string.contact_support),
            ) {
                context.openLink("https://permanent.zohodesk.com/portal/en/newticket")
            }
        }
    }
}
