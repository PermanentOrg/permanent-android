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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.permanent.permanent.models.Archive
import org.permanent.permanent.models.Status
import org.permanent.permanent.models.ThumbStatus
import org.permanent.permanent.ui.composeComponents.ArchiveItem
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun WelcomePage(
    viewModel: ArchiveOnboardingViewModel,
    isTablet: Boolean,
    pagerState: PagerState,
    accountName: String?
) {
    val coroutineScope = rememberCoroutineScope()
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))
    val archives by viewModel.allArchives.collectAsState()

    if (isTablet) {
        TabletBody(
            viewModel, accountName, regularFont, coroutineScope, pagerState, archives
        )
    } else {
        PhoneBody(
            viewModel, accountName, regularFont, coroutineScope, pagerState, archives
        )
    }
}

@Composable
private fun TabletBody(
    viewModel: ArchiveOnboardingViewModel,
    accountName: String?,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    archives: List<Archive>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        horizontalArrangement = Arrangement.spacedBy(64.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            accountName?.let {
                val welcomeTitleText = stringResource(id = R.string.welcome_to_permanent_title, it)
                val start = welcomeTitleText.indexOf(it)
                val spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = start + it.length
                    )
                )

                Text(
                    text = AnnotatedString(text = welcomeTitleText, spanStyles = spanStyles),
                    fontSize = 56.sp,
                    lineHeight = 72.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )

                if (archives.isNotEmpty()) {
                    Text(
                        text = stringResource(id = R.string.welcome_to_permanent_with_archives_description),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color.White,
                        fontFamily = regularFont
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), horizontalAlignment = Alignment.End
        ) {
            if (archives.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.welcome_to_permanent_description),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(archives) { index, archive ->
                    val archiveName = archive.fullName
                    val archiveAccessRole = archive.accessRole

                    if (archiveName != null && archiveAccessRole != null) {
                        ArchiveItem(
                            isTablet = true,
                            isForWelcomePage = true,
                            iconURL = if (archive.thumbStatus == ThumbStatus.OK) archive.thumbURL200 else null,
                            title = archiveName,
                            accessRole = archiveAccessRole,
                            showSubtitle = true,
                            showSeparator = index != archives.lastIndex,
                            showAcceptButton = archive.status == Status.PENDING,
                            showAcceptedLabel = archive.status == Status.OK
                        ) {
                            viewModel.onAcceptBtnClick(archive)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (archives.isNotEmpty()) {
                        CenteredTextAndIconButton(
                            buttonColor = ButtonColor.TRANSPARENT,
                            text = stringResource(id = R.string.create_new_archive),
                            icon = painterResource(id = R.drawable.ic_plus_white)
                        ) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_TYPE.value)
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    CenteredTextAndIconButton(buttonColor = ButtonColor.LIGHT,
                        text = stringResource(id = if (archives.isEmpty()) R.string.get_started else R.string.next),
                        enabled = archives.isEmpty() || archives.any { it.status == Status.OK }) {
                        val nextPage =
                            if (archives.isEmpty()) OnboardingPage.ARCHIVE_TYPE.value else {
                                viewModel.setAcceptedArchiveFlow()
                                OnboardingPage.GOALS.value
                            }

                        coroutineScope.launch {
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneBody(
    viewModel: ArchiveOnboardingViewModel,
    accountName: String?,
    regularFont: FontFamily,
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    archives: List<Archive>
) {
    val scrollState = rememberScrollState()

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 32.dp)
    ) {
        val (content, spacer, buttons) = createRefs()

        Column(modifier = Modifier
            .constrainAs(content) {
                top.linkTo(parent.top)
                bottom.linkTo(spacer.top)
                height = Dimension.fillToConstraints
            }
            .verticalScroll(scrollState)
            .padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {

            accountName?.let {
                val welcomeTitleText = stringResource(id = R.string.welcome_to_permanent_title, it)
                val start = welcomeTitleText.indexOf(it)
                val spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = start,
                        end = start + it.length
                    )
                )

                Text(
                    text = AnnotatedString(text = welcomeTitleText, spanStyles = spanStyles),
                    fontSize = 32.sp,
                    lineHeight = 48.sp,
                    color = Color.White,
                    fontFamily = regularFont
                )
            }

            Text(
                text = stringResource(id = if (archives.isNotEmpty()) R.string.welcome_to_permanent_with_archives_description else R.string.welcome_to_permanent_description),
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                archives.forEachIndexed { index, archive ->
                    val archiveName = archive.fullName
                    val archiveAccessRole = archive.accessRole

                    if (archiveName != null && archiveAccessRole != null) {
                        ArchiveItem(
                            isForWelcomePage = true,
                            title = archiveName,
                            accessRole = archiveAccessRole,
                            showSubtitle = true,
                            showSeparator = index != archives.lastIndex,
                            showAcceptButton = archive.status == Status.PENDING,
                            showAcceptedLabel = archive.status == Status.OK
                        ) {
                            viewModel.onAcceptBtnClick(archive)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier
            .height(24.dp)
            .constrainAs(spacer) {
                bottom.linkTo(buttons.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            })

        Column(modifier = Modifier
            .constrainAs(buttons) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
            .padding(horizontal = 32.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {

            CenteredTextAndIconButton(buttonColor = ButtonColor.LIGHT,
                text = stringResource(id = if (archives.isEmpty()) R.string.get_started else R.string.next),
                enabled = archives.isEmpty() || archives.any { it.status == Status.OK }
            ) {
                val nextPage =
                    if (archives.isEmpty()) OnboardingPage.ARCHIVE_TYPE.value else {
                        viewModel.setAcceptedArchiveFlow()
                        OnboardingPage.GOALS.value
                    }

                coroutineScope.launch {
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            if (archives.isNotEmpty()) {
                CenteredTextAndIconButton(
                    buttonColor = ButtonColor.TRANSPARENT,
                    text = stringResource(id = R.string.new_archive),
                    icon = painterResource(id = R.drawable.ic_plus_white)
                ) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(OnboardingPage.ARCHIVE_TYPE.value)
                    }
                }
            }
        }
    }
}
