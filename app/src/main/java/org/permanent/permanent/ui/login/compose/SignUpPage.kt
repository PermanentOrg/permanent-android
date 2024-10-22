@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.login.compose

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CustomSnackbar
import org.permanent.permanent.viewmodels.AuthenticationViewModel

@Composable
fun SignUpPage(
    viewModel: AuthenticationViewModel, pagerState: PagerState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    var fullNameValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    var emailValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    var passwordValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = ""
            )
        )
    }

    var isUpdatesToggleChecked by remember { mutableStateOf(false) }
    var isTermToggleChecked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Image(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(id = R.string.create_your_new_account),
                    fontSize = 32.sp,
                    lineHeight = 48.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.29f), RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = fullNameValueState,
                        onValueChange = { value -> fullNameValueState = value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.full_name).uppercase(),
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
            }

            item {
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
                            .height(56.dp),
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
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.29f), RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = passwordValueState,
                        onValueChange = { value -> passwordValueState = value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.password).uppercase(),
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = colorResource(id = R.color.whiteUltraTransparent),
                            focusedIndicatorColor = colorResource(id = R.color.whiteUltraTransparent),
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = colorResource(id = R.color.blue400)
                        ),
                        visualTransformation = PasswordVisualTransformation()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(id = R.string.i_agree_to_receive_updates),
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        color = Color.White,
                        fontFamily = regularFont,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isUpdatesToggleChecked,
                        onCheckedChange = { isChecked -> isUpdatesToggleChecked = isChecked },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(id = R.color.white),
                            uncheckedThumbColor = colorResource(id = R.color.white),
                            checkedTrackColor = colorResource(id = R.color.success500),
                            uncheckedTrackColor = colorResource(id = R.color.whiteSuperTransparent),
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val annotatedText = buildAnnotatedString {
                        append(stringResource(id = R.string.i_agree_with))
                        append(" ")

                        pushStringAnnotation(tag = "URL", annotation = BuildConfig.TERMS_URL)
                        withStyle(
                            style = SpanStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(stringResource(id = R.string.terms_and_conditions))
                        }
                        pop()
                    }

                    ClickableText(text = annotatedText, style = TextStyle(
                        color = Color.White,
                        fontFamily = regularFont,
                        fontSize = 14.sp,
                        lineHeight = 24.sp
                    ), modifier = Modifier.weight(1f), onClick = { offset ->
                        annotatedText.getStringAnnotations(
                            tag = "URL", start = offset, end = offset
                        ).firstOrNull()?.let {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                                context.startActivity(intent)
                            }
                    })

                    Spacer(modifier = Modifier.width(24.dp))

                    Switch(
                        checked = isTermToggleChecked,
                        onCheckedChange = { isChecked -> isTermToggleChecked = isChecked },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(id = R.color.white),
                            uncheckedThumbColor = colorResource(id = R.color.white),
                            checkedTrackColor = colorResource(id = R.color.success500),
                            uncheckedTrackColor = colorResource(id = R.color.whiteSuperTransparent),
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.LIGHT,
                    text = stringResource(id = R.string.sign_up),
                    showButtonEnabled = fullNameValueState.text.isNotEmpty() && emailValueState.text.isNotEmpty() && passwordValueState.text.isNotEmpty() && isTermToggleChecked
                ) {
                    keyboardController?.hide()
                    viewModel.clearSnackbar()
//                    viewModel.login(
//                        true, emailValueState.text.trim(), passwordValueState.text.trim()
//                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
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
                        text = stringResource(id = R.string.already_registered).uppercase(),
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
            }

            item {
                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.TRANSPARENT,
                    text = stringResource(id = R.string.sign_in),
                    icon = painterResource(id = R.drawable.ic_key_white)
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(AuthPage.SIGN_IN.value)
                    }
                }
            }
        }

        CustomSnackbar(modifier = Modifier.align(Alignment.BottomCenter),
            isForError = snackbarType == AuthenticationViewModel.SnackbarType.ERROR,
            message = snackbarMessage,
            buttonText = stringResource(id = R.string.ok),
            onButtonClick = {
                viewModel.clearSnackbar()
            })
    }
}
