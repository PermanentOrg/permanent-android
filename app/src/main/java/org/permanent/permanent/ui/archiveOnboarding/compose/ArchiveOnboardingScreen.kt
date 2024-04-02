@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CustomProgressIndicator
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun ArchiveOnboardingScreen(
    viewModel: ArchiveOnboardingViewModel
) {
    val context = LocalContext.current
    val blue900Color = Color(ContextCompat.getColor(context, R.color.blue900))
    val blueLighterColor = Color(ContextCompat.getColor(context, R.color.blueLighter))
    val pagerState = rememberPagerState(initialPage = 0)
    val isTablet = viewModel.isTablet()

    val horizontalPaddingDp = if (isTablet) 64.dp else 32.dp
    val topPaddingDp = if (isTablet) 32.dp else 24.dp
    val spacerPaddingDp = if (isTablet) 32.dp else 8.dp
    val progressIndicatorHeight = if (isTablet) 4.dp else 2.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        blue900Color, blueLighterColor
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = horizontalPaddingDp, end = horizontalPaddingDp, top = topPaddingDp
                ), verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )

            Box(
                modifier = Modifier.padding(top = topPaddingDp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacerPaddingDp)) {
                    OnboardingProgressIndicator(
                        progressIndicatorHeight, horizontalPaddingDp, spacerPaddingDp, 100
                    )

                    OnboardingProgressIndicator(
                        progressIndicatorHeight, horizontalPaddingDp, spacerPaddingDp, 0
                    )

                    OnboardingProgressIndicator(
                        progressIndicatorHeight, horizontalPaddingDp, spacerPaddingDp, 0
                    )
                }
            }

            HorizontalPager(pageCount = 2, state = pagerState, userScrollEnabled = false) { page ->
                if (page == 0) WelcomePage(isTablet, pagerState, viewModel.getAccountName().value)
                else TypeSelectionPage(isTablet, pagerState)
            }
        }
    }
}

@Composable
fun OnboardingProgressIndicator(
    height: Dp, horizontalPaddingDp: Dp, spacerPaddingDp: Dp, percent: Int
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
            .height(height),
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