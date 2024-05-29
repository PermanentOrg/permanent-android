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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
    isTablet: Boolean,
    horizontalPaddingDp: Dp,
    pagerState: PagerState,
    newArchive: NewArchive,
    checkboxStates: CheckboxStates
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    if (isTablet) {
        TabletBody(
            horizontalPaddingDp,
            whiteColor,
            regularFont,
            coroutineScope,
            pagerState,
            newArchive,
            checkboxStates
        )
    } else {
        PhoneBody(
            whiteColor, regularFont, coroutineScope, pagerState, newArchive, checkboxStates
        )
    }
}

@Composable
private fun PhoneBody(
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive,
    checkboxStates: CheckboxStates
) {
    val scrollState = rememberScrollState()

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
                checkedState = checkboxStates.isCaptureChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_digitize),
                checkedState = checkboxStates.isDigitizeChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_collaborate),
                checkedState = checkboxStates.isCollaborateChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_create_an_archive),
                checkedState = checkboxStates.isCreateArchiveChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_share),
                checkedState = checkboxStates.isShareChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_create_a_plan),
                checkedState = checkboxStates.isCreatePlanChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_organize),
                checkedState = checkboxStates.isOrganizeChecked
            )
            CustomCheckbox(
                text = stringResource(id = R.string.goals_something_else),
                checkedState = checkboxStates.isSomethingElseChecked
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
                    newArchive.goals[OnboardingGoal.CAPTURE] = checkboxStates.isCaptureChecked.value
                    newArchive.goals[OnboardingGoal.DIGITIZE] =
                        checkboxStates.isDigitizeChecked.value
                    newArchive.goals[OnboardingGoal.COLLABORATE] =
                        checkboxStates.isCollaborateChecked.value
                    newArchive.goals[OnboardingGoal.CREATE_AN_ARCHIVE] =
                        checkboxStates.isCreateArchiveChecked.value
                    newArchive.goals[OnboardingGoal.SHARE] = checkboxStates.isShareChecked.value
                    newArchive.goals[OnboardingGoal.CREATE_A_PLAN] =
                        checkboxStates.isCreatePlanChecked.value
                    newArchive.goals[OnboardingGoal.ORGANIZE] =
                        checkboxStates.isOrganizeChecked.value
                    newArchive.goals[OnboardingGoal.SOMETHING_ELSE] =
                        checkboxStates.isSomethingElseChecked.value
//                    coroutineScope.launch {
//                        pagerState.animateScrollToPage(OnboardingPage.PRIORITIES_PAGE.value)
//                    }
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
    newArchive: NewArchive,
    checkboxStates: CheckboxStates
) {
    val configuration = LocalConfiguration.current
    val oneThirdOfScreenDp = (configuration.screenWidthDp.dp - 2 * horizontalPaddingDp) / 3
    val spacerWidth = 32.dp

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
                    checkedState = checkboxStates.isCaptureChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_digitize),
                    checkedState = checkboxStates.isDigitizeChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_collaborate),
                    checkedState = checkboxStates.isCollaborateChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_create_an_archive),
                    checkedState = checkboxStates.isCreateArchiveChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_share),
                    checkedState = checkboxStates.isShareChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_create_a_plan),
                    checkedState = checkboxStates.isCreatePlanChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_organize),
                    checkedState = checkboxStates.isOrganizeChecked
                )
            }
            item {
                CustomCheckbox(
                    isTablet = true,
                    text = stringResource(id = R.string.goals_something_else),
                    checkedState = checkboxStates.isSomethingElseChecked
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
                        newArchive.goals[OnboardingGoal.CAPTURE] =
                            checkboxStates.isCaptureChecked.value
                        newArchive.goals[OnboardingGoal.DIGITIZE] =
                            checkboxStates.isDigitizeChecked.value
                        newArchive.goals[OnboardingGoal.COLLABORATE] =
                            checkboxStates.isCollaborateChecked.value
                        newArchive.goals[OnboardingGoal.CREATE_AN_ARCHIVE] =
                            checkboxStates.isCreateArchiveChecked.value
                        newArchive.goals[OnboardingGoal.SHARE] = checkboxStates.isShareChecked.value
                        newArchive.goals[OnboardingGoal.CREATE_A_PLAN] =
                            checkboxStates.isCreatePlanChecked.value
                        newArchive.goals[OnboardingGoal.ORGANIZE] =
                            checkboxStates.isOrganizeChecked.value
                        newArchive.goals[OnboardingGoal.SOMETHING_ELSE] =
                            checkboxStates.isSomethingElseChecked.value
//                        coroutineScope.launch {
//                            pagerState.animateScrollToPage(OnboardingPage.PRIORITIES_PAGE.value)
//                        }
                    }
                }
            }
        }
    }
}

data class CheckboxStates(
    val isCaptureChecked: MutableState<Boolean>,
    val isDigitizeChecked: MutableState<Boolean>,
    val isCollaborateChecked: MutableState<Boolean>,
    val isCreateArchiveChecked: MutableState<Boolean>,
    val isShareChecked: MutableState<Boolean>,
    val isCreatePlanChecked: MutableState<Boolean>,
    val isOrganizeChecked: MutableState<Boolean>,
    val isSomethingElseChecked: MutableState<Boolean>
) {
    companion object {
        fun create() = CheckboxStates(
            isCaptureChecked = mutableStateOf(false),
            isDigitizeChecked = mutableStateOf(false),
            isCollaborateChecked = mutableStateOf(false),
            isCreateArchiveChecked = mutableStateOf(false),
            isShareChecked = mutableStateOf(false),
            isCreatePlanChecked = mutableStateOf(false),
            isOrganizeChecked = mutableStateOf(false),
            isSomethingElseChecked = mutableStateOf(false)
        )
    }
}