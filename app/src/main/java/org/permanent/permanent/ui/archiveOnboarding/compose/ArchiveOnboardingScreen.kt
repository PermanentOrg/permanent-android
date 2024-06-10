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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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

@Composable
fun ArchiveOnboardingScreen(
    viewModel: ArchiveOnboardingViewModel
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = OnboardingPage.WELCOME_PAGE.value,
        pageCount = { OnboardingPage.values().size })
    val isTablet = viewModel.isTablet()

    val blue900Color = remember { Color(ContextCompat.getColor(context, R.color.blue900)) }
    val blueLighterColor = remember { Color(ContextCompat.getColor(context, R.color.blueLighter)) }

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
                goals = mutableStateListOf(),
                priorities = mutableStateListOf()
            )
        )
    }

    val isSecondProgressBarEmpty by viewModel.isSecondProgressBarEmpty.collectAsState()
    val isThirdProgressBarEmpty by viewModel.isThirdProgressBarEmpty.collectAsState()
    val goals = rememberSaveable(
        saver = listSaver(
            save = {
                it.map { goal ->
                    listOf(
                        goal.type.ordinal,
                        goal.description,
                        goal.isChecked.value
                    )
                }
            },
            restore = { restoredGoals ->
                restoredGoals.map {
                    OnboardingGoal(
                        type = OnboardingGoalType.values()[it[0] as Int],
                        description = it[1] as String,
                        isChecked = mutableStateOf(it[2] as Boolean)
                    )
                }.toMutableStateList()
            }
        )
    ) {
        mutableStateListOf(
            OnboardingGoal(
                OnboardingGoalType.CAPTURE,
                context.getString(R.string.goals_capture),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.DIGITIZE,
                context.getString(R.string.goals_digitize),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.COLLABORATE,
                context.getString(R.string.goals_collaborate),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.CREATE_AN_ARCHIVE,
                context.getString(R.string.goals_create_an_archive),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.SHARE,
                context.getString(R.string.goals_share),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.CREATE_A_PLAN,
                context.getString(R.string.goals_create_a_plan),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.ORGANIZE,
                context.getString(R.string.goals_organize),
                mutableStateOf(false)
            ),
            OnboardingGoal(
                OnboardingGoalType.SOMETHING_ELSE,
                context.getString(R.string.goals_something_else),
                mutableStateOf(false)
            ),
        )
    }

    val priorities = rememberSaveable(
        saver = listSaver(
            save = {
                it.map { priority ->
                    listOf(
                        priority.type.ordinal,
                        priority.description,
                        priority.isChecked.value
                    )
                }
            },
            restore = { restoredPriorities ->
                restoredPriorities.map {
                    OnboardingPriority(
                        type = OnboardingPriorityType.values()[it[0] as Int],
                        description = it[1] as String,
                        isChecked = mutableStateOf(it[2] as Boolean)
                    )
                }.toMutableStateList()
            }
        )
    ) {
        mutableStateListOf(
            OnboardingPriority(
                OnboardingPriorityType.ACCESS,
                context.getString(R.string.priorities_access),
                mutableStateOf(false)
            ),
            OnboardingPriority(
                OnboardingPriorityType.SUPPORTING,
                context.getString(R.string.priorities_supporting),
                mutableStateOf(false)
            ),
            OnboardingPriority(
                OnboardingPriorityType.PRESERVING,
                context.getString(R.string.priorities_preserving),
                mutableStateOf(false)
            ),
            OnboardingPriority(
                OnboardingPriorityType.PROFESSIONAL,
                context.getString(R.string.priorities_professional),
                mutableStateOf(false)
            ),
            OnboardingPriority(
                OnboardingPriorityType.COLLABORATE,
                context.getString(R.string.priorities_collaborate),
                mutableStateOf(false)
            ),
            OnboardingPriority(
                OnboardingPriorityType.INTEREST,
                context.getString(R.string.priorities_interest),
                mutableStateOf(false)
            ),
        )
    }

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            OnboardingPage.ARCHIVE_NAME_PAGE.value -> viewModel.updateSecondProgressBarEmpty(true)

            OnboardingPage.GOALS_PAGE.value -> {
                viewModel.updateSecondProgressBarEmpty(false)
                viewModel.updateThirdProgressBarEmpty(true)
            }

            OnboardingPage.PRIORITIES_PAGE.value -> viewModel.updateThirdProgressBarEmpty(false)
        }
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
                .padding(top = topPaddingDp),
            verticalArrangement = Arrangement.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(40.dp)
                    .padding(horizontal = horizontalPaddingDp)
            )

            Box(
                modifier = Modifier.padding(
                    top = topPaddingDp, start = horizontalPaddingDp, end = horizontalPaddingDp
                )
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacerPaddingDp)) {
                    OnboardingProgressIndicator(
                        progressIndicatorHeight,
                        horizontalPaddingDp,
                        spacerPaddingDp,
                        isEmpty = false
                    )

                    OnboardingProgressIndicator(
                        progressIndicatorHeight,
                        horizontalPaddingDp,
                        spacerPaddingDp,
                        isSecondProgressBarEmpty
                    )

                    OnboardingProgressIndicator(
                        progressIndicatorHeight,
                        horizontalPaddingDp,
                        spacerPaddingDp,
                        isThirdProgressBarEmpty
                    )
                }
            }

            HorizontalPager(
                state = pagerState, userScrollEnabled = false
            ) { page ->
                when (page) {
                    OnboardingPage.WELCOME_PAGE.value -> {
                        WelcomePage(
                            isTablet = isTablet,
                            pagerState = pagerState,
                            accountName = viewModel.getAccountName().value
                        )
                    }

                    OnboardingPage.ARCHIVE_TYPE_PAGE.value -> {
                        ArchiveTypePage(isTablet = isTablet,
                            pagerState = pagerState,
                            onArchiveTypeClick = { type: ArchiveType, typeName: String ->
                                newArchive = newArchive.copy(type = type, typeName = typeName)
                            })
                    }

                    OnboardingPage.ARCHIVE_NAME_PAGE.value -> {
                        ArchiveNamePage(
                            isTablet = isTablet, pagerState = pagerState, newArchive = newArchive
                        )
                    }

                    OnboardingPage.GOALS_PAGE.value -> {
                        GoalsPage(
                            isTablet = isTablet,
                            horizontalPaddingDp = horizontalPaddingDp,
                            pagerState = pagerState,
                            newArchive = newArchive,
                            goals
                        )
                    }

                    OnboardingPage.PRIORITIES_PAGE.value -> {
                        PrioritiesPage(
                            isTablet = isTablet,
                            horizontalPaddingDp = horizontalPaddingDp,
                            pagerState = pagerState,
                            newArchive = newArchive,
                            priorities
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingProgressIndicator(
    height: Dp, horizontalPaddingDp: Dp, spacerPaddingDp: Dp, isEmpty: Boolean
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
        if (isEmpty) 0 else 100
    )
}

data class NewArchive(
    var type: ArchiveType,
    var typeName: String,
    var name: String,
    var goals: List<OnboardingGoal>,
    var priorities: List<OnboardingPriority>
)

enum class OnboardingPage(val value: Int) {
    WELCOME_PAGE(0), ARCHIVE_TYPE_PAGE(1), ARCHIVE_NAME_PAGE(2), GOALS_PAGE(3), PRIORITIES_PAGE(4)
}
