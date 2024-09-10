@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.login.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomSnackbar
import org.permanent.permanent.viewmodels.LoginFragmentViewModel

@Composable
fun LoginContainer(
    viewModel: LoginFragmentViewModel
) {
    val pagerState = rememberPagerState(initialPage = LoginPage.SIGN_IN.value,
        pageCount = { LoginPage.values().size })
    val isTablet = viewModel.isTablet()

    val isBusyState by viewModel.isBusyState.collectAsState()
    val errorMessage by viewModel.showError.collectAsState()

    val horizontalPaddingDp = if (isTablet) 64.dp else 32.dp
    val configuration = LocalConfiguration.current
    val oneThirdOfScreenDp = (configuration.screenWidthDp.dp - 2 * horizontalPaddingDp) / 3

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colorResource(id = R.color.blue900),
                            colorResource(id = R.color.blueLighter)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPaddingDp),
            ) {
                Row {
                    if (isTablet) {
                        Image(
                            painter = painterResource(id = R.drawable.img_sign_in),
                            contentDescription = "Sign In Image",
                            modifier = Modifier
                                .size(2 * oneThirdOfScreenDp + horizontalPaddingDp)
                                .padding(horizontal = horizontalPaddingDp)
                        )
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    HorizontalPager(
                        state = pagerState, userScrollEnabled = false
                    ) { page ->
                        when (page) {
                            LoginPage.SIGN_IN.value -> {
                                SignInPage(
                                    viewModel = viewModel, pagerState = pagerState
                                )
                            }

                            LoginPage.CODE_VERIFICATION.value -> {
//                                CodeVerificationPage(
//                                    viewModel = viewModel,
//                                    isTablet = isTablet,
//                                    pagerState = pagerState
//                                )
                            }

                            LoginPage.SIGN_UP.value -> {
//                                SignUpPage(
//                                    viewModel = viewModel,
//                                    isTablet = isTablet,
//                                    pagerState = pagerState
//                                )
                            }

                            LoginPage.FORGOT_PASSWORD.value -> {
//                                ForgotPasswordPage(
//                                    viewModel = viewModel,
//                                    isTablet = isTablet,
//                                    pagerState = pagerState
//                                )
                            }
                        }
                    }
                }
                Image(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .padding(horizontal = horizontalPaddingDp)
                )
            }
        }

        // Overlay with spinning images
        if (isBusyState) {
            CircularProgressIndicator()
        }

        if (errorMessage.isNotEmpty()) {
            CustomSnackbar(isTablet = isTablet,
                message = errorMessage,
                buttonText = "OK",
                modifier = Modifier.align(Alignment.BottomCenter),
                onButtonClick = {
                    viewModel.clearError()
                })
        }
    }
}

enum class LoginPage(val value: Int) {
    SIGN_IN(0), CODE_VERIFICATION(1), SIGN_UP(2), FORGOT_PASSWORD(3)
}
