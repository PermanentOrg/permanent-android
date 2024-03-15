@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonStyle
import org.permanent.permanent.ui.composeComponents.CustomProgressIndicator
import org.permanent.permanent.ui.composeComponents.TextAndIconButton
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun ArchiveOnboardingScreen(
    viewModel: ArchiveOnboardingViewModel
) {
    val context = LocalContext.current
    val horizontalPaddingDp = 32.dp
    val spacerPaddingDp = 8.dp
    val blue900Color = Color(ContextCompat.getColor(context, R.color.blue900))
    val blueLighterColor = Color(ContextCompat.getColor(context, R.color.blueLighter))
    val pagerState = rememberPagerState(initialPage = 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        blue900Color,
                        blueLighterColor
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPaddingDp, vertical = 24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Next",
                modifier = Modifier.size(40.dp)
            )

            Box(
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacerPaddingDp)) {
                    OnboardingProgressIndicator(
                        horizontalPaddingDp, spacerPaddingDp, 100
                    )

                    OnboardingProgressIndicator(
                        horizontalPaddingDp, spacerPaddingDp, 0
                    )

                    OnboardingProgressIndicator(
                        horizontalPaddingDp, spacerPaddingDp, 0
                    )
                }
            }

            HorizontalPager(pageCount = 2, state = pagerState, userScrollEnabled = false) { page ->
                if (page == 0) WelcomeOnboardingPage(
                    horizontalPaddingDp,
                    pagerState,
                    viewModel.getAccountName().value
                )
                else TypeSelectionPage(horizontalPaddingDp)
            }
        }
    }
}

@Composable
fun OnboardingProgressIndicator(
    horizontalPaddingDp: Dp, spacerPaddingDp: Dp, percent: Int
) {
    val context = LocalContext.current
    val whiteSuperTransparentColor =
        Color(ContextCompat.getColor(context, R.color.whiteSuperExtraTransparent))
    val purpleColor = Color(ContextCompat.getColor(context, R.color.barneyPurple))
    val accentColor = Color(ContextCompat.getColor(context, R.color.colorAccent))

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    CustomProgressIndicator(
        Modifier
            .clip(shape = RoundedCornerShape(3.dp))
            .height(2.dp),
        (screenWidthDp - horizontalPaddingDp - horizontalPaddingDp - spacerPaddingDp - spacerPaddingDp) / 3,
        whiteSuperTransparentColor,
        Brush.horizontalGradient(
            listOf(
                purpleColor, accentColor
            )
        ),
        percent
    )
}

@Composable
fun WelcomeOnboardingPage(
    horizontalPaddingDp: Dp,
    pagerState: PagerState,
    accountName: String?
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = horizontalPaddingDp)
    ) {
        accountName?.let {
            val welcomeTitleText = stringResource(id = R.string.welcome_to_permanent_title,
                "$it"
            )
            val start = welcomeTitleText.indexOf(it)
            val spanStyles = listOf(
                AnnotatedString.Range(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    start = start,
                    end = start + "$it".length
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
            ButtonStyle.LIGHT,
            text = stringResource(id = R.string.get_started),
            showButtonEnabled = true
        ) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(1)
            }
        }
    }
}

@Composable
fun TypeSelectionPage(horizontalPaddingDp: Dp) {
    val context = LocalContext.current
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = horizontalPaddingDp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.create_your_first_archive_title),
            fontSize = 32.sp,
            lineHeight = 48.sp,
            color = whiteColor,
            fontFamily = regularFont
        )
    }
}