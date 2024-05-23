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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.composeComponents.CustomProgressIndicator
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel
import java.util.EnumMap

@Composable
fun ArchiveOnboardingScreen(
    viewModel: ArchiveOnboardingViewModel
) {
    val context = LocalContext.current
    val blue900Color = Color(ContextCompat.getColor(context, R.color.blue900))
    val blueLighterColor = Color(ContextCompat.getColor(context, R.color.blueLighter))
    val pagerState = rememberPagerState(initialPage = OnboardingPage.WELCOME_PAGE.value)
    val isTablet = viewModel.isTablet()

    val horizontalPaddingDp = if (isTablet) 64.dp else 32.dp
    val topPaddingDp = if (isTablet) 32.dp else 24.dp
    val spacerPaddingDp = if (isTablet) 32.dp else 8.dp
    val progressIndicatorHeight = if (isTablet) 4.dp else 2.dp

    var newArchive by remember {
        mutableStateOf(
            NewArchive(
                type = ArchiveType.PERSON,
                typeName = context.getString(R.string.personal),
                name = "",
                goals = EnumMap(OnboardingGoal::class.java),
                priorities = ""
            )
        )
    }

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

            HorizontalPager(
                pageCount = OnboardingPage.values().size,
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    OnboardingPage.WELCOME_PAGE.value -> WelcomePage(
                        isTablet = isTablet,
                        pagerState = pagerState,
                        accountName = viewModel.getAccountName().value
                    )

                    OnboardingPage.ARCHIVE_TYPE_PAGE.value -> ArchiveTypePage(isTablet = isTablet,
                        pagerState = pagerState,
                        onArchiveTypeClick = { type: ArchiveType, typeName: String ->
                            val archive = NewArchive(
                                type = type,
                                typeName = typeName,
                                name = "",
                                goals = EnumMap(OnboardingGoal::class.java),
                                priorities = ""
                            )
                            newArchive = archive
                        })

                    OnboardingPage.ARCHIVE_NAME_PAGE.value -> ArchiveNamePage(
                        isTablet = isTablet,
                        pagerState = pagerState,
                        newArchive = newArchive
                    )

                    OnboardingPage.GOALS_PAGE.value -> GoalsPage(
                        isTablet = isTablet,
                        horizontalPaddingDp = horizontalPaddingDp,
                        pagerState = pagerState,
                        newArchive = newArchive
                    )
                }
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

data class NewArchive(
    var type: ArchiveType,
    var typeName: String,
    var name: String,
    var goals: EnumMap<OnboardingGoal, Boolean>,
    var priorities: String?
)

enum class OnboardingGoal {
    CAPTURE,
    DIGITIZE,
    COLLABORATE,
    CREATE_AN_ARCHIVE,
    SHARE,
    CREATE_A_PLAN,
    ORGANIZE,
    SOMETHING_ELSE
}

enum class OnboardingPage(val value: Int) {
    WELCOME_PAGE(0),
    ARCHIVE_TYPE_PAGE(1),
    ARCHIVE_NAME_PAGE(2),
    GOALS_PAGE(3),
    PRIORITIES_PAGE(4)
}