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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.composeComponents.ArchiveItem
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.SmallTextAndIconButton
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

@Composable
fun CongratulationsPage(
    viewModel: ArchiveOnboardingViewModel, isTablet: Boolean
) {
    val regularFont = FontFamily(Font(R.font.open_sans_regular_ttf))

    val archives by viewModel.archives.collectAsState()

    if (isTablet) {
        TabletBody(
            viewModel, regularFont, archives
        )
    } else {
        PhoneBody(
            viewModel, regularFont, archives
        )
    }
}

@Composable
private fun PhoneBody(
    viewModel: ArchiveOnboardingViewModel, regularFont: FontFamily, archives: List<Archive>
) {
    val scrollState = rememberScrollState()
    val uriHandler = LocalUriHandler.current

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

            Text(
                text = stringResource(id = R.string.congratulations_title),
                fontSize = 32.sp,
                lineHeight = 48.sp,
                color = Color.White,
                fontFamily = regularFont
            )

            val annotatedString = buildAnnotatedString {
                append(stringResource(id = R.string.congratulations_description_first))
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = Color.White, textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(id = R.string.congratulations_description_second))
                }
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://permanent.zohodesk.com/portal/en/kb/permanent-legacy-foundation",
                    start = this.length - stringResource(id = R.string.congratulations_description_second).length,
                    end = this.length
                )
            }

            ClickableText(text = annotatedString, style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            ), onClick = { offset ->
                annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()
                    ?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
            })

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                archives.forEach { archive ->
                    val archiveName = archive.fullName
                    val archiveAccessRole = archive.accessRole

                    if (archiveName != null && archiveAccessRole != null) {
                        ArchiveItem(
                            title = archiveName,
                            accessRole = archiveAccessRole,
                            showSubtitle = archiveAccessRole != AccessRole.OWNER
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.constrainAs(divider) {
                bottom.linkTo(spacer.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }, thickness = 1.dp, color = Color.White.copy(alpha = 0.16f)
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
                    buttonColor = ButtonColor.LIGHT,
                    text = stringResource(id = R.string.done),
                    icon = painterResource(id = R.drawable.ic_done_white),
                ) {
                    viewModel.completeArchiveOnboarding()
                }
            }
        }
    }
}

@Composable
private fun TabletBody(
    viewModel: ArchiveOnboardingViewModel, regularFont: FontFamily, archives: List<Archive>
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        horizontalArrangement = Arrangement.spacedBy(64.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = stringResource(id = R.string.congratulations_title),
                fontSize = 56.sp,
                lineHeight = 72.sp,
                color = Color.White,
                fontFamily = regularFont
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), horizontalAlignment = Alignment.End
        ) {
            val annotatedString = buildAnnotatedString {
                append(stringResource(id = R.string.congratulations_description_first))
                append(" ")
                withStyle(
                    style = SpanStyle(
                        color = Color.White, textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(stringResource(id = R.string.congratulations_description_second))
                }
                addStringAnnotation(
                    tag = "URL",
                    annotation = "https://permanent.zohodesk.com/portal/en/kb/permanent-legacy-foundation",
                    start = this.length - stringResource(id = R.string.congratulations_description_second).length,
                    end = this.length
                )
            }

            ClickableText(text = annotatedString, style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = Color.White,
                fontFamily = regularFont
            ), onClick = { offset ->
                annotatedString.getStringAnnotations("URL", offset, offset).firstOrNull()
                    ?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
            })

            archives.forEach { archive ->
                val archiveName = archive.fullName
                val archiveAccessRole = archive.accessRole

                if (archiveName != null && archiveAccessRole != null) {
                    ArchiveItem(
                        isTablet = true,
                        title = archiveName,
                        accessRole = archiveAccessRole,
                        showSubtitle = archiveAccessRole != AccessRole.OWNER
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1.0f))

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    SmallTextAndIconButton(
                        buttonColor = ButtonColor.LIGHT,
                        text = stringResource(id = R.string.done),
                        icon = painterResource(id = R.drawable.ic_done_white),
                    ) {
                        viewModel.completeArchiveOnboarding()
                    }
                }
            }
        }
    }
}