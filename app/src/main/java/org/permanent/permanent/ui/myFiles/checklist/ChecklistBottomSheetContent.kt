@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.myFiles.checklist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.network.models.ChecklistItem
import org.permanent.permanent.ui.composeComponents.AnimatedSnackbar
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.composeComponents.SnackbarType
import org.permanent.permanent.viewmodels.ChecklistViewModel
import kotlin.math.roundToInt

@Composable
fun ChecklistBottomSheetContent(viewModel: ChecklistViewModel, onClose: () -> Unit) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val checklistItems by viewModel.checklistItems.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarType by viewModel.snackbarType.collectAsState()
    val pagerState = rememberPagerState(initialPage = ChecklistPage.BODY.value,
        pageCount = { ChecklistPage.values().size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        ChecklistHeader(onMinimizeClick = onClose)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isBusyState -> {
                    CircularProgressIndicator(overlayColor = OverlayColor.LIGHT)
                }

                else -> {
                    HorizontalPager(
                        state = pagerState,
                        beyondBoundsPageCount = 2,
                        userScrollEnabled = false,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            ChecklistPage.BODY.value -> {
                                ChecklistBodyPage(viewModel = viewModel,
                                    checklistItems = checklistItems,
                                    onItemClick = { viewModel.onChecklistItemClicked(it) },
                                    onDismissForeverClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(ChecklistPage.CONFIRMATION.value)
                                        }
                                    })
                            }

                            ChecklistPage.CONFIRMATION.value -> {
                                ConfirmationPage(onConfirmClick = {
                                    viewModel.dismissForeverChecklist {
                                        onClose()
                                    }
                                }, onDismissClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(ChecklistPage.BODY.value)
                                    }
                                })
                            }
                        }
                    }
                }
            }

            AnimatedSnackbar(modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
                isForError = snackbarType == SnackbarType.ERROR,
                message = snackbarMessage,
                buttonText = stringResource(id = R.string.ok),
                onButtonClick = {
                    viewModel.clearSnackbar()
                })
        }
    }
}

@Composable
fun ChecklistHeader(
    onMinimizeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                colorResource(R.color.blue25), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with white background circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_checklist_white),
                contentDescription = "Checklist Icon",
                tint = colorResource(R.color.success500),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Texts
        Column(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .padding(top = 4.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(R.string.getting_started), style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.getting_started_description), style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue600),
                )
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Minimize Icon
        IconButton(onClick = onMinimizeClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_minus_blue),
                contentDescription = "Minimize",
                tint = colorResource(R.color.blue900),
            )
        }
    }
}

@Composable
fun ChecklistBodyPage(
    viewModel: ChecklistViewModel,
    checklistItems: List<ChecklistItem>,
    onItemClick: (ChecklistItem) -> Unit,
    onDismissForeverClick: () -> Unit
) {
    val completedCount = checklistItems.count { it.completed }
    val progress =
        if (checklistItems.isNotEmpty()) completedCount.toFloat() / checklistItems.size else 0f
    val progressPercent = (progress * 100).roundToInt()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 24.dp, end = 24.dp)
        ) {
            // Header: "Account set up" and % value
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.account_set_up), style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.blue600)
                    ), modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$progressPercent%", style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily(Font(R.font.usual_medium)),
                        color = colorResource(R.color.blue900)
                    )
                )
            }

            // Progress bar
            RoundedLinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )

            // Checklist items
            checklistItems.forEach { item ->
                ChecklistItemRow(
                    item = item,
                    viewModel = viewModel,
                    onClick = { onItemClick(item) })
            }
        }

        // Spacer to push divider to bottom of remaining space
        Spacer(modifier = Modifier.weight(1f))

        // Custom divider
        HorizontalDivider(
            thickness = 1.dp, color = colorResource(R.color.blue50)
        )

        // Dismiss row
        Row(modifier = Modifier
            .clickable { onDismissForeverClick() }
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Icon(
                modifier = Modifier.padding(horizontal = 8.dp),
                painter = painterResource(R.drawable.ic_eye_off_blue),
                contentDescription = null,
                tint = colorResource(R.color.blue900)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.dont_show_again), style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue900)
                )
            )
        }
    }
}

@Composable
fun ChecklistItemRow(
    item: ChecklistItem, viewModel: ChecklistViewModel, onClick: () -> Unit
) {

    val iconRes = viewModel.getIconForItem(item.id)

    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(vertical = if (item.completed) 11.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.padding(horizontal = 8.dp),
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (item.completed) colorResource(R.color.blue400) else colorResource(R.color.blue900)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title, style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = if (item.completed) colorResource(R.color.blue400) else colorResource(R.color.blue900),
                textDecoration = if (item.completed) TextDecoration.LineThrough else null
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        if (item.completed) {
            // Icon with white background circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(colorResource(R.color.lightGreen), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle_green),
                    contentDescription = "Checklist Icon",
                    tint = colorResource(R.color.success500)
                )
            }
        } else {
            Icon(
                modifier = Modifier.padding(end = 12.dp),
                painter = painterResource(R.drawable.ic_arrow_select_light_blue),
                contentDescription = null,
                tint = colorResource(R.color.colorPrimary200)
            )
        }
    }
}

@Composable
fun RoundedLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = colorResource(R.color.blue100),
    progressColor: Color = colorResource(R.color.success500)
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = progressColor,
            trackColor = Color.Transparent,
        )
    }
}

@Composable
fun ConfirmationPage(
    onConfirmClick: () -> Unit, onDismissClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .align(Alignment.CenterHorizontally),
            painter = painterResource(R.drawable.ic_eye_off),
            contentDescription = null,
            tint = colorResource(R.color.blue400)
        )

        Text(
            text = stringResource(R.string.dont_show_again_description),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue700)
            )
        )

        // Spacer to push buttons to bottom of remaining space
        Spacer(modifier = Modifier.weight(1f))

        CenteredTextAndIconButton(
            buttonColor = ButtonColor.DARK,
            text = stringResource(id = R.string.yes_button),
            icon = null,
            onButtonClick = onConfirmClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        CenteredTextAndIconButton(
            buttonColor = ButtonColor.LIGHT_BLUE,
            text = stringResource(id = R.string.button_cancel),
            icon = null,
            onButtonClick = onDismissClick
        )
    }
}

enum class ChecklistPage(val value: Int) {
    BODY(0), SUCCESS(1), CONFIRMATION(2), ERROR(3)
}