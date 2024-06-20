@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.TextAndIconButton

@Composable
fun WelcomePage(
    isTablet: Boolean,
    pagerState: PagerState,
    accountName: String?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    if (isTablet) {
        TabletBody(accountName, whiteColor, regularFont, coroutineScope, pagerState)
    } else {
        PhoneBody(accountName, whiteColor, regularFont, coroutineScope, pagerState)
    }
}

@Composable
private fun TabletBody(
    accountName: String?,
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        horizontalArrangement = Arrangement.spacedBy(64.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            accountName?.let {
                val welcomeTitleText = stringResource(id = R.string.welcome_to_permanent_title, it)
                val start = welcomeTitleText.indexOf(it)
                val spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = start + it.length
                    )
                )

                Text(
                    text = AnnotatedString(text = welcomeTitleText, spanStyles = spanStyles),
                    fontSize = 56.sp,
                    lineHeight = 72.sp,
                    color = whiteColor,
                    fontFamily = regularFont
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(id = R.string.welcome_to_permanent_description),
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = whiteColor,
                fontFamily = regularFont
            )

            Spacer(modifier = Modifier.weight(1.0f))

            Box(
                modifier = Modifier
                    .width(168.dp)
            ) {
                TextAndIconButton(
                    ButtonColor.LIGHT,
                    text = stringResource(id = R.string.get_started),
                    showButtonEnabled = true
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_TYPE_PAGE.value)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneBody(
    accountName: String?,
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(32.dp)
    ) {
        accountName?.let {
            val welcomeTitleText = stringResource(id = R.string.welcome_to_permanent_title, it)
            val start = welcomeTitleText.indexOf(it)
            val spanStyles = listOf(
                AnnotatedString.Range(
                    SpanStyle(fontWeight = FontWeight.Bold), start = start, end = start + it.length
                )
            )

            Text(
                text = AnnotatedString(text = welcomeTitleText, spanStyles = spanStyles),
                fontSize = 32.sp,
                lineHeight = 48.sp,
                color = whiteColor,
                fontFamily = regularFont
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.welcome_to_permanent_description),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = whiteColor,
            fontFamily = regularFont
        )

        Spacer(modifier = Modifier.weight(1.0f))

        TextAndIconButton(
            ButtonColor.LIGHT,
            text = stringResource(id = R.string.get_started),
            showButtonEnabled = true
        ) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_TYPE_PAGE.value)
            }
        }
    }
}

@Preview
@Composable
fun WelcomePagePhonePreview() {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { OnboardingPage.values().size })
    WelcomePage(false, pagerState, "Jane Doe")
}