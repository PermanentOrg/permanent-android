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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.models.ArchiveType
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.CustomLinearProgressIndicator
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun ArchiveOnboardingScreen(
    viewModel: ArchiveOnboardingViewModel
) {
    val context = LocalContext.current

    val pagerState = rememberPagerState(initialPage = OnboardingPage.WELCOME.value,
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

    val isFirstProgressBarEmpty by viewModel.isFirstProgressBarEmpty.collectAsState()
    val isSecondProgressBarEmpty by viewModel.isSecondProgressBarEmpty.collectAsState()
    val isThirdProgressBarEmpty by viewModel.isThirdProgressBarEmpty.collectAsState()
    val isBusyState by viewModel.isBusyState.collectAsState()
    val errorMessage by viewModel.showError.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarEventFlow = remember { MutableSharedFlow<String>() }
    val snackbarHostState = remember { SnackbarHostState() }

    val goals = rememberSaveable(saver = listSaver(save = {
        it.map { goal ->
            listOf(
                goal.type.ordinal, goal.description, goal.isChecked.value
            )
        }
    }, restore = { restoredGoals ->
        restoredGoals.map {
            OnboardingGoal(
                type = OnboardingGoalType.values()[it[0] as Int],
                description = it[1] as String,
                isChecked = mutableStateOf(it[2] as Boolean)
            )
        }.toMutableStateList()
    })) {
        viewModel.createOnboardingGoals(context).map { (ordinal, description) ->
            OnboardingGoal(
                type = OnboardingGoalType.values()[ordinal],
                description = description,
                isChecked = mutableStateOf(false)
            )
        }.toMutableStateList()
    }

    val priorities = rememberSaveable(saver = listSaver(save = {
        it.map { priority ->
            listOf(
                priority.type.ordinal, priority.description, priority.isChecked.value
            )
        }
    }, restore = { restoredPriorities ->
        restoredPriorities.map {
            OnboardingPriority(
                type = OnboardingPriorityType.values()[it[0] as Int],
                description = it[1] as String,
                isChecked = mutableStateOf(it[2] as Boolean)
            )
        }.toMutableStateList()
    })) {
        viewModel.createOnboardingPriorities(context).map { (ordinal, description) ->
            OnboardingPriority(
                type = OnboardingPriorityType.values()[ordinal],
                description = description,
                isChecked = mutableStateOf(false)
            )
        }.toMutableStateList()
    }

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            OnboardingPage.ARCHIVE_NAME.value -> {
                viewModel.updateFirstProgressBarEmpty(false)
                viewModel.updateSecondProgressBarEmpty(true)
            }

            OnboardingPage.GOALS.value -> {
                viewModel.updateFirstProgressBarEmpty(true)
                viewModel.updateSecondProgressBarEmpty(false)
                viewModel.updateThirdProgressBarEmpty(true)
            }

            OnboardingPage.PRIORITIES.value -> {
                viewModel.updateSecondProgressBarEmpty(true)
                viewModel.updateThirdProgressBarEmpty(false)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                            isFirstProgressBarEmpty
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
                    state = pagerState, beyondBoundsPageCount = 2 , userScrollEnabled = false
                ) { page ->
                    when (page) {
                        OnboardingPage.WELCOME.value -> {
                            WelcomePage(
                                viewModel = viewModel,
                                isTablet = isTablet,
                                pagerState = pagerState,
                                accountName = viewModel.getAccountName().value
                            )
                        }

                        OnboardingPage.ARCHIVE_TYPE.value -> {
                            ArchiveTypePage(isTablet = isTablet,
                                pagerState = pagerState,
                                onArchiveTypeClick = { type: ArchiveType, typeName: String ->
                                    newArchive = newArchive.copy(type = type, typeName = typeName)
                                })
                        }

                        OnboardingPage.ARCHIVE_NAME.value -> {
                            ArchiveNamePage(
                                isTablet = isTablet,
                                pagerState = pagerState,
                                newArchive = newArchive
                            )
                        }

                        OnboardingPage.GOALS.value -> {
                            GoalsPage(
                                viewModel = viewModel,
                                isTablet = isTablet,
                                pagerState = pagerState,
                                newArchive = newArchive,
                                goals = goals
                            )
                        }

                        OnboardingPage.PRIORITIES.value -> {
                            PrioritiesPage(
                                viewModel = viewModel,
                                isTablet = isTablet,
                                pagerState = pagerState,
                                newArchive = newArchive,
                                priorities = priorities
                            )
                        }

                        OnboardingPage.CONGRATULATIONS.value -> {
                            CongratulationsPage(
                                viewModel = viewModel,
                                isTablet = isTablet
                            )
                        }
                    }
                }
            }
        }

        // Overlay with spinning images
        if (isBusyState) {
            CircularProgressIndicator()
        }
    }

    LaunchedEffect(errorMessage) {
        coroutineScope.launch {
            if (errorMessage.isNotEmpty())
                snackbarHostState.showSnackbar(errorMessage)
        }
    }

    LaunchedEffect(snackbarEventFlow) {
        snackbarEventFlow.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    SnackbarHost(hostState = snackbarHostState)
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

    CustomLinearProgressIndicator(
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
    WELCOME(0), ARCHIVE_TYPE(1), ARCHIVE_NAME(2), GOALS(3), PRIORITIES(4), CONGRATULATIONS(5)
}
