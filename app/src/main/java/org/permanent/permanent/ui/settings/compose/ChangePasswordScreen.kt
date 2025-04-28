package org.permanent.permanent.ui.settings.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.AnimatedSnackbar
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomLinearProgressIndicator
import org.permanent.permanent.ui.composeComponents.PasswordInputField
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.ui.settings.compose.twoStepVerification.rememberKeyboardVisibility
import org.permanent.permanent.viewmodels.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(viewModel: ChangePasswordViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val isKeyboardVisible = rememberKeyboardVisibility()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isBusyState by viewModel.isBusyState.collectAsState()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var retypedNewPassword by remember { mutableStateOf("") }

    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()

    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage.isNotEmpty()) {
            delay(4000) // wait 4 seconds
            viewModel.clearSnackbar()
        }
    }

    val strength = getPasswordStrength(newPassword)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.blue25))
                .padding(32.dp)
                .verticalScroll(scrollState)
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(
                visible = !isKeyboardVisible,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.secure_your_account).uppercase(),
                        fontSize = 10.sp,
                        lineHeight = 24.sp,
                        color = colorResource(id = R.color.blue900),
                        fontFamily = FontFamily(Font(R.font.usual_medium))
                    )

                    Text(
                        text = stringResource(id = R.string.change_password_description),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = colorResource(id = R.color.blue900),
                        fontFamily = FontFamily(Font(R.font.usual_medium))
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    HorizontalDivider(thickness = 1.dp, color = colorResource(id = R.color.blue50))

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            Text(
                text = stringResource(id = R.string.current_password).uppercase(),
                fontSize = 10.sp,
                lineHeight = 8.sp,
                color = colorResource(id = R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                letterSpacing = 1.6.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                showToggle = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.new_password).uppercase(),
                fontSize = 10.sp,
                lineHeight = 8.sp,
                color = colorResource(id = R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                letterSpacing = 1.6.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputField(value = newPassword, onValueChange = { newPassword = it })

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = newPassword.length >= 8,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)) {

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        PasswordProgressIndicator(
                            color = strength.color, isEmpty = false
                        )

                        PasswordProgressIndicator(
                            color = strength.color, isEmpty = strength == PasswordStrength.WEAK
                        )

                        PasswordProgressIndicator(
                            color = strength.color,
                            isEmpty = strength == PasswordStrength.WEAK || strength == PasswordStrength.MEDIUM
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(id = strength.label),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colorResource(id = strength.color),
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.retype_new_password).uppercase(),
                fontSize = 10.sp,
                lineHeight = 8.sp,
                color = colorResource(id = R.color.blue900),
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                letterSpacing = 1.6.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordInputField(
                value = retypedNewPassword,
                onValueChange = { retypedNewPassword = it })

            Spacer(modifier = Modifier.height(32.dp))

            CenteredTextAndIconButton(buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.change_password),
                icon = null,
                onButtonClick = {
                    keyboardController?.hide()
                    when {
                        currentPassword.isBlank() -> {
                            viewModel.showSnackbar(
                                message = context.getString(R.string.current_password_required),
                                type = SnackbarType.ERROR
                            )
                        }

                        newPassword.length < 8 -> {
                            viewModel.showSnackbar(
                                message = context.getString(R.string.password_min_length_error),
                                type = SnackbarType.ERROR
                            )
                        }

                        newPassword != retypedNewPassword -> {
                            viewModel.showSnackbar(
                                message = context.getString(R.string.passwords_do_not_match),
                                type = SnackbarType.ERROR
                            )
                        }

                        newPassword == currentPassword -> {
                            viewModel.showSnackbar(
                                message = context.getString(R.string.new_password_same_as_current),
                                type = SnackbarType.ERROR
                            )
                        }

                        else -> {

                            viewModel.changePassword(currentPassword,
                                newPassword,
                                retypedNewPassword,
                                onSuccess = {
                                    currentPassword = ""
                                    newPassword = ""
                                    retypedNewPassword = ""
                                })
                        }
                    }
                })
        }

        // Overlay with spinning images
        if (isBusyState) {
            CircularProgressIndicator()
        }

        AnimatedSnackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            isForError = snackbarType == SnackbarType.ERROR,
            message = snackbarMessage,
            showButton = false
        )
    }
}

enum class PasswordStrength(val label: Int, val color: Int) {
    WEAK(R.string.password_weak, R.color.error500), MEDIUM(
        R.string.password_medium, R.color.warning500
    ),
    STRONG(R.string.password_strong, R.color.success500)
}

fun getPasswordStrength(password: String): PasswordStrength {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        4 -> PasswordStrength.STRONG
        2, 3 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.WEAK
    }
}

@Composable
fun PasswordProgressIndicator(
    color: Int, isEmpty: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val horizontalPaddingDp = 56.dp
    val spacerPaddingDp = 4.dp
    val foregroundColor = colorResource(id = color)

    CustomLinearProgressIndicator(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(3.dp))
            .height(4.dp),
        width = (screenWidthDp - horizontalPaddingDp - horizontalPaddingDp - spacerPaddingDp - spacerPaddingDp) / 3,
        backgroundColor = colorResource(R.color.blue50),
        foregroundColor = Brush.horizontalGradient(
            listOf(
                foregroundColor, foregroundColor
            )
        ),
        percent = if (isEmpty) 0 else 100
    )
}