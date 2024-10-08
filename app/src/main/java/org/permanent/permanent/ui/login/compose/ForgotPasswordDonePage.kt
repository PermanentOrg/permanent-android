@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.login.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton

@Composable
fun ForgotPasswordDonePage(
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = stringResource(id = R.string.forgot_your_password_title),
            fontSize = 32.sp,
            lineHeight = 48.sp,
            color = Color.White,
            fontFamily = regularFont
        )

        Text(
            text = stringResource(id = R.string.forgot_your_password_done_text),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = Color.White,
            fontFamily = regularFont
        )

        CenteredTextAndIconButton(
            buttonColor = ButtonColor.TRANSPARENT,
            text = stringResource(id = R.string.go_to_sign_in),
        ) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(AuthPage.SIGN_IN.value)
            }
        }
    }
}