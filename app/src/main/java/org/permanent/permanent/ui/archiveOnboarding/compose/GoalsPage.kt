@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.archiveOnboarding.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CustomCheckbox
import org.permanent.permanent.ui.composeComponents.SmallTextAndIconButton
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel


@Composable
fun GoalsPage(
    viewModel: ArchiveOnboardingViewModel,
    isTablet: Boolean,
    pagerState: PagerState,
    newArchive: NewArchive,
    goals: SnapshotStateList<OnboardingGoal>
) {
    val coroutineScope = rememberCoroutineScope()
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    if (isTablet) {
        TabletBody(
            viewModel, regularFont, coroutineScope, pagerState, newArchive, goals
        )
    } else {
        PhoneBody(
            viewModel, regularFont, coroutineScope, pagerState, newArchive, goals
        )
    }
}

@Composable
private fun PhoneBody(
    viewModel: ArchiveOnboardingViewModel,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive,
    goals: SnapshotStateList<OnboardingGoal>
) {
    val scrollState = rememberScrollState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp)
    ) {
        val (content, divider, spacer, buttons) = createRefs()

        Column(modifier = Modifier
            .constrainAs(content) {
                top.linkTo(parent.top)
                bottom.linkTo(divider.top)
                height = Dimension.fillToConstraints
            }
            .verticalScroll(scrollState)
            .padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {

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
                color = Color.White,
                fontFamily = regularFont
            )

            Text(
                text = stringResource(id = R.string.chart_your_path_description),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                goals.forEach { goal ->
                    CustomCheckbox(
                        text = goal.description, checkedState = goal.isChecked
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.constrainAs(divider) {
                bottom.linkTo(spacer.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }, thickness = 1.dp, color = colorResource(id = R.color.whiteSuperExtraTransparent)
        )

        Spacer(modifier = Modifier
            .height(32.dp)
            .constrainAs(spacer) {
                bottom.linkTo(buttons.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            })

        Row(modifier = Modifier
            .constrainAs(buttons) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
            .padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {

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
                    val backPage =
                        if (viewModel.isAcceptedArchiveFlow.value) OnboardingPage.WELCOME.value else OnboardingPage.ARCHIVE_NAME.value
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(backPage)
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
                    newArchive.goals = goals
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.PRIORITIES.value)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabletBody(
    viewModel: ArchiveOnboardingViewModel,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive,
    goals: SnapshotStateList<OnboardingGoal>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp),
        verticalArrangement = Arrangement.spacedBy(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f)
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
                    color = Color.White,
                    fontFamily = regularFont
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.chart_your_path_description),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )
            }
        }

        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            goals.forEach { goal ->
                item {
                    CustomCheckbox(
                        isTablet = true, text = goal.description, checkedState = goal.isChecked
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {}

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        SmallTextAndIconButton(
                            buttonColor = ButtonColor.TRANSPARENT,
                            text = stringResource(id = R.string.back),
                            icon = painterResource(id = R.drawable.ic_arrow_back_rounded_white),
                            iconAlignment = ButtonIconAlignment.START
                        ) {
                            val backPage =
                                if (viewModel.isAcceptedArchiveFlow.value) OnboardingPage.WELCOME.value else OnboardingPage.ARCHIVE_NAME.value
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(backPage)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        SmallTextAndIconButton(
                            buttonColor = ButtonColor.LIGHT,
                            text = stringResource(id = R.string.next)
                        ) {
                            newArchive.goals = goals
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(OnboardingPage.PRIORITIES.value)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class OnboardingGoal(
    val type: OnboardingGoalType, val description: String, val isChecked: MutableState<Boolean>
)

enum class OnboardingGoalType {
    CAPTURE, DIGITIZE, COLLABORATE, PUBLISH, SHARE, LEGACY, ORGANIZE, UNDEFINED
}

val OnboardingGoalSaver: Saver<OnboardingGoal, *> =
    listSaver(save = { listOf(it.type.ordinal, it.description, it.isChecked.value) }, restore = {
        OnboardingGoal(
            type = OnboardingGoalType.values()[it[0] as Int],
            description = it[1] as String,
            isChecked = mutableStateOf(it[2] as Boolean)
        )
    })