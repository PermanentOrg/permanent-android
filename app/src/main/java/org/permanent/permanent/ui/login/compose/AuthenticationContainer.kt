@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.login.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomTextButton
import org.permanent.permanent.ui.openLink
import org.permanent.permanent.viewmodels.AuthenticationViewModel

@Composable
fun AuthenticationContainer(
    viewModel: AuthenticationViewModel
) {
    val pagerState = rememberPagerState(
        initialPage = AuthPage.SIGN_IN.value,
        pageCount = { AuthPage.values().size })

    val navigateToPage by viewModel.navigateToPage.collectAsState()

    LaunchedEffect(navigateToPage) {
        navigateToPage?.let { page ->
            pagerState.animateScrollToPage(page.value)
            viewModel.clearPageNavigation()
        }
    }

    val isTablet = viewModel.isTablet()

    val isBusyState by viewModel.isBusyState.collectAsState()

    val horizontalPaddingDp = if (isTablet) 64.dp else 32.dp

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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPaddingDp)
            ) {
                if (isTablet) {
                    LeftSideView(horizontalPaddingDp)

                    Spacer(modifier = Modifier.width(64.dp))
                }

                HorizontalPager(
                    state = pagerState, beyondViewportPageCount = 3, userScrollEnabled = false
                ) { page ->
                    when (page) {
                        AuthPage.SIGN_IN.value -> {
                            SignInPage(viewModel = viewModel, pagerState = pagerState)
                        }

                        AuthPage.CODE_VERIFICATION.value -> {
                            CodeVerificationPage(viewModel = viewModel)
                        }

                        AuthPage.SIGN_UP.value -> {
                            SignUpPage(viewModel = viewModel, pagerState = pagerState)
                        }

                        AuthPage.FORGOT_PASSWORD.value -> {
                            ForgotPasswordPage(viewModel = viewModel, pagerState = pagerState)
                        }

                        AuthPage.FORGOT_PASSWORD_DONE.value -> {
                            ForgotPasswordDonePage(pagerState = pagerState)
                        }

                        AuthPage.BIOMETRICS.value -> {
                            BiometricsPage(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        if (isBusyState) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun LeftSideView(
    horizontalPaddingDp: Dp
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val oneThirdOfScreenDp = (configuration.screenWidthDp.dp - 2 * horizontalPaddingDp) / 3

    Box(
        modifier = Modifier
            .width(2 * oneThirdOfScreenDp)
            .fillMaxHeight()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_sign_in),
            contentDescription = "Sign In Image",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalPaddingDp,
                    end = horizontalPaddingDp,
                    bottom = horizontalPaddingDp
                )
                .align(Alignment.BottomCenter)
                .background(
                    colorResource(id = R.color.whiteLightTransparent),
                    shape = RoundedCornerShape(size = 12.dp)
                )
        ) {
            Column {
                Text(
                    modifier = Modifier.padding(top = 32.dp, start = 32.dp, end = 32.dp),
                    text = stringResource(id = R.string.haadsma_dairy_truck_title).uppercase(),
                    color = colorResource(id = R.color.blue900),
                    fontSize = 10.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_semibold_ttf)),
                    letterSpacing = 1.6.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp),
                    text = stringResource(id = R.string.haadsma_dairy_truck_text),
                    color = colorResource(id = R.color.blue900),
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.open_sans_regular_ttf)),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .padding(start = 8.dp)
                ) {
                    CustomTextButton(
                        style = ButtonColor.DARK,
                        text = stringResource(id = R.string.start_exploring_now),
                        icon = painterResource(id = R.drawable.ic_arrow_next_rounded_primary)
                    ) {
                        context.openLink("https://www.permanent.org/gallery")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

enum class AuthPage(val value: Int) {
    SIGN_IN(0), CODE_VERIFICATION(1), SIGN_UP(2), FORGOT_PASSWORD(3), FORGOT_PASSWORD_DONE(4), BIOMETRICS(5)
}
