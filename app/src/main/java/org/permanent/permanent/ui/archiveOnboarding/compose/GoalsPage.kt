@file:OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CustomCheckbox
import org.permanent.permanent.ui.composeComponents.SmallTextAndIconButton


@Composable
fun GoalsPage(
    isTablet: Boolean, horizontalPaddingDp: Dp, pagerState: PagerState, newArchive: NewArchive
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    if (isTablet) {
        TabletBody(
            horizontalPaddingDp, whiteColor, regularFont, coroutineScope, pagerState, newArchive
        )
    } else {
        PhoneBody(
            whiteColor, regularFont, coroutineScope, pagerState, newArchive
        )
    }
}

@Composable
private fun PhoneBody(
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive
) {
    val scrollState = rememberScrollState()
    val captureCheckedState = remember { mutableStateOf(false) }
    val digitizeCheckedState = remember { mutableStateOf(false) }
    val collaborateCheckedState = remember { mutableStateOf(false) }
    val createArchiveCheckedState = remember { mutableStateOf(false) }
    val shareCheckedState = remember { mutableStateOf(false) }
    val createPlanCheckedState = remember { mutableStateOf(false) }
    val organizeCheckedState = remember { mutableStateOf(false) }
    val somethingElseCheckedState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 32.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val titleText = stringResource(id = R.string.chart_your_path_title)
        val boldedWord = "path"
        val start = titleText.indexOf(boldedWord)
        val spanStyles = listOf(
            AnnotatedString.Range(
                SpanStyle(fontWeight = FontWeight.Bold),
                start = start,
                end = start + boldedWord.length
            )
        )

        Text(
            text = AnnotatedString(text = titleText, spanStyles = spanStyles),
            fontSize = 32.sp,
            lineHeight = 48.sp,
            color = whiteColor,
            fontFamily = regularFont
        )

        Text(
            text = stringResource(id = R.string.chart_your_path_description),
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = whiteColor,
            fontFamily = regularFont
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CustomCheckbox(
                isTablet = false,
                text = stringResource(id = R.string.goals_capture),
                checkedState = captureCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_digitize),
                checkedState = digitizeCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_collaborate),
                checkedState = collaborateCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_create_an_archive),
                checkedState = createArchiveCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_share), checkedState = shareCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_create_a_plan),
                checkedState = createPlanCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_organize),
                checkedState = organizeCheckedState
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_something_else),
                checkedState = somethingElseCheckedState
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SmallTextAndIconButton(
                    buttonColor = ButtonColor.TRANSPARENT,
                    text = stringResource(id = R.string.back),
                    icon = painterResource(id = R.drawable.ic_arrow_back_rounded_white),
                    iconAlignment = ButtonIconAlignment.START
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_NAME_PAGE.value)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                SmallTextAndIconButton(
                    buttonColor = ButtonColor.LIGHT, text = stringResource(id = R.string.next)
                ) {
                    newArchive.goals[OnboardingGoal.CAPTURE] = captureCheckedState.value
                    newArchive.goals[OnboardingGoal.DIGITIZE] = digitizeCheckedState.value
                    newArchive.goals[OnboardingGoal.COLLABORATE] = collaborateCheckedState.value
                    newArchive.goals[OnboardingGoal.CREATE_AN_ARCHIVE] =
                        createArchiveCheckedState.value
                    newArchive.goals[OnboardingGoal.SHARE] = shareCheckedState.value
                    newArchive.goals[OnboardingGoal.CREATE_A_PLAN] = createPlanCheckedState.value
                    newArchive.goals[OnboardingGoal.ORGANIZE] = organizeCheckedState.value
                    newArchive.goals[OnboardingGoal.SOMETHING_ELSE] =
                        somethingElseCheckedState.value
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.PRIORITIES_PAGE.value)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletBody(
    horizontalPaddingDp: Dp,
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive
) {
    val configuration = LocalConfiguration.current
    val oneThirdOfScreenDp = (configuration.screenWidthDp.dp - 2 * horizontalPaddingDp) / 3
    val spacerWidth = 32.dp

    val captureCheckedState = remember { mutableStateOf(false) }
    val digitizeCheckedState = remember { mutableStateOf(false) }
    val collaborateCheckedState = remember { mutableStateOf(false) }
    val createArchiveCheckedState = remember { mutableStateOf(false) }
    val shareCheckedState = remember { mutableStateOf(false) }
    val createPlanCheckedState = remember { mutableStateOf(false) }
    val organizeCheckedState = remember { mutableStateOf(false) }
    val somethingElseCheckedState = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 64.dp),
        verticalArrangement = Arrangement.spacedBy(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.End,
        ) {
            Column(
                modifier = Modifier.width(2 * (oneThirdOfScreenDp) - spacerWidth / 2),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                val titleText = stringResource(id = R.string.chart_your_path_title)
                val boldedWord = "path"
                val start = titleText.indexOf(boldedWord)
                val spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = start + boldedWord.length
                    )
                )

                Text(
                    text = AnnotatedString(text = titleText, spanStyles = spanStyles),
                    fontSize = 56.sp,
                    lineHeight = 72.sp,
                    color = whiteColor,
                    fontFamily = regularFont
                )
            }

            Spacer(modifier = Modifier.width(spacerWidth))

            Column(
                modifier = Modifier.width(oneThirdOfScreenDp - spacerWidth / 2),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(id = R.string.chart_your_path_description),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = whiteColor,
                    fontFamily = regularFont
                )
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_capture),
                    checkedState = captureCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_digitize),
                    checkedState = digitizeCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_collaborate),
                    checkedState = collaborateCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_create_an_archive),
                    checkedState = createArchiveCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_share),
                    checkedState = shareCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_create_a_plan),
                    checkedState = createPlanCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_organize),
                    checkedState = organizeCheckedState
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_something_else),
                    checkedState = somethingElseCheckedState
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.End,
        ) {
            Row(
                modifier = Modifier.width(oneThirdOfScreenDp - spacerWidth / 2)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    SmallTextAndIconButton(
                        buttonColor = ButtonColor.TRANSPARENT,
                        text = stringResource(id = R.string.back),
                        icon = painterResource(id = R.drawable.ic_arrow_back_rounded_white),
                        iconAlignment = ButtonIconAlignment.START
                    ) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_NAME_PAGE.value)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    SmallTextAndIconButton(
                        buttonColor = ButtonColor.LIGHT, text = stringResource(id = R.string.next)
                    ) {
                        newArchive.goals[OnboardingGoal.CAPTURE] = captureCheckedState.value
                        newArchive.goals[OnboardingGoal.DIGITIZE] = digitizeCheckedState.value
                        newArchive.goals[OnboardingGoal.COLLABORATE] = collaborateCheckedState.value
                        newArchive.goals[OnboardingGoal.CREATE_AN_ARCHIVE] =
                            createArchiveCheckedState.value
                        newArchive.goals[OnboardingGoal.SHARE] = shareCheckedState.value
                        newArchive.goals[OnboardingGoal.CREATE_A_PLAN] =
                            createPlanCheckedState.value
                        newArchive.goals[OnboardingGoal.ORGANIZE] = organizeCheckedState.value
                        newArchive.goals[OnboardingGoal.SOMETHING_ELSE] =
                            somethingElseCheckedState.value
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(OnboardingPage.PRIORITIES_PAGE.value)
                        }
                    }
                }
            }
        }
    }
}
