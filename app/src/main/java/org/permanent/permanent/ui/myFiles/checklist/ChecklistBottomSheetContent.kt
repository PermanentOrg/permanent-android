@file:OptIn(ExperimentalFoundationApi::class)

package org.permanent.permanent.ui.myFiles.checklist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.permanent.permanent.R
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.myFiles.checklist.compose.ChecklistBodyPage
import org.permanent.permanent.ui.myFiles.checklist.compose.ChecklistCompletedPage
import org.permanent.permanent.ui.myFiles.checklist.compose.ChecklistConfirmationPage
import org.permanent.permanent.ui.myFiles.checklist.compose.ChecklistErrorPage
import org.permanent.permanent.viewmodels.ChecklistViewModel

@Composable
fun ChecklistBottomSheetContent(
    viewModel: ChecklistViewModel, onClose: () -> Unit, onHideChecklistButton: () -> Unit
) {
    val isBusyState by viewModel.isBusyState.collectAsState()
    val checklistItems by viewModel.checklistItems.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val pagerState = rememberPagerState(initialPage = ChecklistPage.BODY.value,
        pageCount = { ChecklistPage.values().size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentPage) {
        if (pagerState.currentPage != currentPage.value) {
            pagerState.animateScrollToPage(currentPage.value)
        }
    }

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
                                ChecklistConfirmationPage(onConfirmClick = {
                                    viewModel.dismissForeverChecklist {
                                        onHideChecklistButton()
                                    }
                                }, onDismissClick = {
                                    val allCompleted = checklistItems.all { it.completed }
                                    val targetPage =
                                        if (allCompleted) ChecklistPage.COMPLETED else ChecklistPage.BODY
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(targetPage.value)
                                    }
                                })
                            }

                            ChecklistPage.COMPLETED.value -> {
                                ChecklistCompletedPage(onDismissForeverClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(ChecklistPage.CONFIRMATION.value)
                                    }
                                })
                            }

                            ChecklistPage.ERROR.value -> {
                                ChecklistErrorPage(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
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
                text = stringResource(R.string.finish_account_setup), style = TextStyle(
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

enum class ChecklistPage(val value: Int) {
    BODY(0), COMPLETED(1), CONFIRMATION(2), ERROR(3)
}