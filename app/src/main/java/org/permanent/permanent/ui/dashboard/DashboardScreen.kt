package org.permanent.permanent.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.AnimatedTemporarySnackbar
import org.permanent.permanent.ui.composeComponents.TemporarySnackbarType
import org.permanent.permanent.ui.dashboard.widgets.ChartYourPathWidget
import org.permanent.permanent.ui.dashboard.widgets.CreateArchiveBottomSheet
import org.permanent.permanent.ui.dashboard.widgets.CreateArchiveWidget
import org.permanent.permanent.ui.dashboard.widgets.GreetingWidget
import org.permanent.permanent.ui.dashboard.widgets.ImportantToYouWidget
import org.permanent.permanent.viewmodels.DashboardViewModel

/**
 * The widget-based Dashboard. Renders the ViewModel's ordered widget list, dispatching each
 * [DashboardWidgetType] to its self-contained composable. Shows a loading skeleton first
 * (Figma "Dashboard MVP / Loading…" frame).
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onGoToArchive: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val firstName by viewModel.firstName.collectAsState()
    val createArchiveState by viewModel.createArchiveState.collectAsState()
    val prioritiesState by viewModel.prioritiesState.collectAsState()
    val chartPathState by viewModel.chartPathState.collectAsState()
    val errorMessage by viewModel.showError.collectAsState()
    val savedMessage by viewModel.savedMessage.collectAsState()
    val warningMessage by viewModel.warningMessage.collectAsState()

    var showCreateSheet by remember { mutableStateOf(false) }

    // When creation succeeds, dismiss the sheet so the success card is visible underneath.
    LaunchedEffect(createArchiveState) {
        if (createArchiveState is CreateArchiveState.Success) showCreateSheet = false
    }

    // One temporary notification, driven by whichever message is active (error > warning > success).
    val (snackbarMessage, snackbarType) = when {
        errorMessage.isNotEmpty() -> errorMessage to TemporarySnackbarType.ERROR
        warningMessage.isNotEmpty() -> warningMessage to TemporarySnackbarType.WARNING
        savedMessage.isNotEmpty() -> savedMessage to TemporarySnackbarType.SUCCESS
        else -> "" to TemporarySnackbarType.NONE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.blue25))
    ) {
        if (isLoading) {
            DashboardLoadingSkeleton()
        } else {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val viewportHeight = maxHeight
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Stretch to at least the viewport so the footer can sit at the bottom when
                    // few widgets remain; grows past it (and scrolls) when content is tall.
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = viewportHeight)
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            viewModel.widgets.forEachIndexed { index, widget ->
                                // 24dp sits between cards. For the dismissable widgets it lives
                                // inside their AnimatedVisibility so it collapses with the card,
                                // leaving no empty slot once the exit animation finishes.
                                val topGap = if (index == 0) 0.dp else 24.dp
                                when (widget) {
                                    DashboardWidgetType.GREETING -> {
                                        if (topGap > 0.dp) Spacer(Modifier.height(topGap))
                                        GreetingWidget(firstName = firstName)
                                    }
                                    DashboardWidgetType.CREATE_ARCHIVE -> {
                                        if (topGap > 0.dp) Spacer(Modifier.height(topGap))
                                        CreateArchiveWidget(
                                            state = createArchiveState,
                                            onCreateClick = { showCreateSheet = true },
                                            onGoToArchiveClick = onGoToArchive
                                        )
                                    }
                                    DashboardWidgetType.IMPORTANT_TO_YOU -> AnimatedVisibility(
                                        visible = prioritiesState != WidgetActionState.Done,
                                        exit = shrinkVertically(
                                            animationSpec = tween(300),
                                            shrinkTowards = Alignment.Top
                                        ) + fadeOut(tween(300))
                                    ) {
                                        Column {
                                            if (topGap > 0.dp) Spacer(Modifier.height(topGap))
                                            ImportantToYouWidget(
                                                state = prioritiesState,
                                                onSave = { tags -> viewModel.savePriorities(tags) },
                                                onRemindLater = { viewModel.dismissPriorities() }
                                            )
                                        }
                                    }
                                    DashboardWidgetType.CHART_PATH -> AnimatedVisibility(
                                        visible = chartPathState != WidgetActionState.Done,
                                        exit = shrinkVertically(
                                            animationSpec = tween(300),
                                            shrinkTowards = Alignment.Top
                                        ) + fadeOut(tween(300))
                                    ) {
                                        Column {
                                            if (topGap > 0.dp) Spacer(Modifier.height(topGap))
                                            ChartYourPathWidget(
                                                state = chartPathState,
                                                onSave = { tags -> viewModel.saveGoals(tags) },
                                                onRemindLater = { viewModel.dismissGoals() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // Footer that closes the feed (Figma node 25369:22267). SpaceBetween pins
                        // it to the bottom when content is short; the top padding keeps a 24dp gap
                        // from the last widget when content fills the screen.
                        Text(
                            text = stringResource(R.string.dashboard_all_caught_up),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                brush = Brush.linearGradient(PurpleOrangeTitleGradient),
                                fontFamily = UsualFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            }
        }

        AnimatedTemporarySnackbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            type = snackbarType,
            message = snackbarMessage,
            onButtonClick = {
                viewModel.onErrorShown()
                viewModel.onSavedMessageShown()
                viewModel.onWarningShown()
            }
        )
    }

    if (showCreateSheet) {
        CreateArchiveBottomSheet(
            isCreating = createArchiveState == CreateArchiveState.Creating,
            onCreate = { name, type -> viewModel.createArchive(name, type) },
            onDismiss = { showCreateSheet = false }
        )
    }
}

/**
 * Loading placeholder mirroring the Figma "Dashboard MVP / Loading…" frame (node 25365-21987):
 * a greeting row (avatar + two text lines) above two tall card placeholders, all shimmering.
 */
@Composable
private fun DashboardLoadingSkeleton() {
    val shimmer = rememberShimmerBrush()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Greeting placeholder: avatar + two stacked text lines.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(shimmer)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(shimmer)
                )
                Box(
                    modifier = Modifier
                        .width(185.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }
        }
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(shimmer)
            )
        }
    }
}

/** Animated left-to-right shimmer brush shared by the loading-skeleton placeholders. */
@Composable
private fun rememberShimmerBrush(): Brush {
    val base = Color(0xFFE7E8ED)      // Permanent Blue/50
    val highlight = Color(0xFFF7F8FC) // lighter sweep band
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )
    return Brush.linearGradient(
        colors = listOf(base, highlight, base),
        start = Offset(x, 0f),
        end = Offset(x + 300f, 0f)
    )
}
