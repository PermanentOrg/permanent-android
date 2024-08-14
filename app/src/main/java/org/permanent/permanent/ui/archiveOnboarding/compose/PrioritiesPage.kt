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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CustomCheckbox
import org.permanent.permanent.ui.composeComponents.SmallTextAndIconButton
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun PrioritiesPage(
    viewModel: ArchiveOnboardingViewModel,
    isTablet: Boolean,
    horizontalPaddingDp: Dp,
    pagerState: PagerState,
    newArchive: NewArchive,
    priorities: SnapshotStateList<OnboardingPriority>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val whiteColor = Color(ContextCompat.getColor(context, R.color.white))
    val whiteSuperTransparentColor =
        Color(ContextCompat.getColor(context, R.color.whiteSuperExtraTransparent))

    val newArchiveCallsSuccess by viewModel.newArchiveCallsSuccess.collectAsState()

    LaunchedEffect(newArchiveCallsSuccess) {
        if (newArchiveCallsSuccess) {
            pagerState.animateScrollToPage(OnboardingPage.CONGRATULATIONS.value)
            viewModel.resetNewArchiveCallsSuccess()
        }
    }

    if (isTablet) {
        TabletBody(
            viewModel,
            horizontalPaddingDp,
            whiteColor,
            regularFont,
            coroutineScope,
            pagerState,
            newArchive,
            priorities
        )
    } else {
        PhoneBody(
            viewModel,
            whiteSuperTransparentColor,
            whiteColor,
            regularFont,
            coroutineScope,
            pagerState,
            newArchive,
            priorities
        )
    }
}

@Composable
private fun PhoneBody(
    viewModel: ArchiveOnboardingViewModel,
    whiteSuperTransparentColor: Color,
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive,
    priorities: SnapshotStateList<OnboardingPriority>
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
            val titleText = stringResource(id = R.string.tell_us_title)
            val boldedWord = "important"
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
                text = stringResource(id = R.string.tell_us_description),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = whiteColor,
                fontFamily = regularFont
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                priorities.forEach { goal ->
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
            }, thickness = 1.dp, color = whiteSuperTransparentColor
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
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.GOALS.value)
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
                    newArchive.priorities = priorities
                    viewModel.onNextButtonClick(newArchive)
                }
            }
        }
    }
}

@Composable
private fun TabletBody(
    viewModel: ArchiveOnboardingViewModel,
    horizontalPaddingDp: Dp,
    whiteColor: Color,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    newArchive: NewArchive,
    priorities: SnapshotStateList<OnboardingPriority>
) {
    val configuration = LocalConfiguration.current
    val oneThirdOfScreenDp = (configuration.screenWidthDp.dp - 2 * horizontalPaddingDp) / 3
    val spacerWidth = 32.dp

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
            horizontalArrangement = Arrangement.End,
        ) {
            Column(
                modifier = Modifier.width(2 * (oneThirdOfScreenDp) - spacerWidth / 2),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                val titleText = stringResource(id = R.string.tell_us_title)
                val boldedWord = "important"
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
                    text = stringResource(id = R.string.tell_us_description),
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
            priorities.forEach { priority ->
                item {
                    CustomCheckbox(
                        isTablet = true,
                        text = priority.description,
                        checkedState = priority.isChecked
                    )
                }
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
                            pagerState.animateScrollToPage(OnboardingPage.GOALS.value)
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
                        newArchive.priorities = priorities
                        viewModel.onNextButtonClick(newArchive)
                    }
                }
            }
        }
    }
}


data class OnboardingPriority(
    val type: OnboardingPriorityType, val description: String, val isChecked: MutableState<Boolean>
)

enum class OnboardingPriorityType {
    SAFE, NONPROFIT, GENEALOGY, PROFESSIONAL, COLLABORATE, DIGIPRES
}

val OnboardingPrioritySaver: Saver<OnboardingPriority, *> =
    listSaver(save = { listOf(it.type.ordinal, it.description, it.isChecked.value) }, restore = {
        OnboardingPriority(
            type = OnboardingPriorityType.values()[it[0] as Int],
            description = it[1] as String,
            isChecked = mutableStateOf(it[2] as Boolean)
        )
    })