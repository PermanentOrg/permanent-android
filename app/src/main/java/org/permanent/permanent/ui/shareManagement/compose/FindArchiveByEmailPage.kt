package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.ButtonIconAlignment
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.viewmodels.FindArchiveByEmailUiState
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun FindArchiveByEmailPage(
    viewModel: ShareManagementViewModel,
    isCurrentPage: Boolean,
    onClose: () -> Unit,
) {
    val emailQuery by viewModel.emailQuery.collectAsState()
    val state by viewModel.findByEmailState.collectAsState()
    val accessedArchiveIds by viewModel.accessedArchiveIds.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Re-focus the field and show the keyboard whenever this page becomes the
    // current pager page (initial open and when returning from Grant access).
    LaunchedEffect(isCurrentPage) {
        if (isCurrentPage) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // The no-account / invite state sits on a blue25 background in the designs,
    // the other states on white.
    val backgroundColor = if (state is FindArchiveByEmailUiState.NoResults) {
        colorResource(R.color.blue25)
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.find_archive_using_email),
                onBackBtnClick = {
                    keyboardController?.hide()
                    viewModel.onBackBtnClick(SharePage.FIND_ARCHIVE_BY_EMAIL)
                },
                onCloseClick = {
                    keyboardController?.hide()
                    onClose()
                }
            )

            EmailSearchField(
                value = emailQuery,
                onValueChange = { viewModel.onEmailQueryChange(it) },
                onSearch = {
                    keyboardController?.hide()
                    viewModel.onEmailSearchSubmit()
                },
                onClear = { viewModel.onEmailQueryChange("") },
                focusRequester = focusRequester
            )

            Box(modifier = Modifier.weight(1f)) {
                when (val s = state) {
                    is FindArchiveByEmailUiState.Found -> ResultsList(
                        archives = s.archives,
                        accessedArchiveIds = accessedArchiveIds,
                        onArchiveClick = {
                            keyboardController?.hide()
                            viewModel.onArchiveResultClick(it)
                        }
                    )

                    is FindArchiveByEmailUiState.NoResults -> NoAccountInviteSection(
                        email = s.email,
                        onInviteClick = {
                            keyboardController?.hide()
                            viewModel.onInviteNowClick(s.email)
                        }
                    )

                    is FindArchiveByEmailUiState.Error -> CenteredMessage(text = s.message)

                    FindArchiveByEmailUiState.Idle -> Unit
                }
            }

            PastSharesRow(onClick = { viewModel.onPastSharesClick() })
        }
    }
}

@Composable
private fun EmailSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
) {
    val primaryColor = colorResource(R.color.colorPrimary)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 24.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = primaryColor,
                spotColor = primaryColor
            )
            .background(colorResource(R.color.white), RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.blue50),
                shape = RoundedCornerShape(12.dp)
            )
            .height(48.dp)
            .padding(start = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorResource(R.color.blue25)),
            contentAlignment = Alignment.Center
        ) {
            GradientSearchIcon()
        }

        Box(modifier = Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue900),
                ),
                cursorBrush = SolidColor(primaryColor),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(R.string.email_address_hint),
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 24.sp,
                                fontFamily = FontFamily(Font(R.font.usual_regular)),
                                color = colorResource(R.color.blue400),
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (value.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onClear() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(colorResource(R.color.blue200)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_white),
                        contentDescription = "Clear",
                        tint = colorResource(R.color.white),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GradientSearchIcon() {
    val brush = Brush.linearGradient(
        colors = listOf(colorResource(R.color.barneyPurple), colorResource(R.color.colorAccent)),
        start = Offset(0f, 0f),
        end = Offset(48f, 48f)
    )
    Icon(
        painter = painterResource(id = R.drawable.ic_search_middle_grey),
        contentDescription = null,
        tint = Color.Unspecified,
        modifier = Modifier
            .size(22.dp)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            .drawWithContent {
                drawContent()
                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
            }
    )
}

@Composable
private fun ResultsList(
    archives: List<Archive>,
    accessedArchiveIds: Set<Int>,
    onArchiveClick: (Archive) -> Unit
) {
    // Archives that already have access are pushed to the bottom (stable within each group).
    val orderedArchives = remember(archives, accessedArchiveIds) {
        archives.sortedBy { it.id in accessedArchiveIds }
    }
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(orderedArchives, key = { it.id }) { archive ->
            ArchiveResultRow(
                archive = archive,
                hasAccess = archive.id in accessedArchiveIds,
                onClick = { onArchiveClick(archive) }
            )
        }
    }
}

@Composable
private fun ArchiveResultRow(archive: Archive, hasAccess: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasAccess) Modifier else Modifier.clickable { onClick() })
            .padding(start = 24.dp, end = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            ArchiveThumbnail(archive)
            if (hasAccess) {
                // De-emphasize the thumbnail with a white 16% scrim (Figma: White / 16%).
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = archive.fullName ?: "",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(if (hasAccess) R.color.blue400 else R.color.blue900),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (hasAccess) {
                Text(
                    text = stringResource(R.string.already_has_access_to_this_share),
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.success500),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(
                id = if (hasAccess) R.drawable.ic_check_circle_filled
                else R.drawable.ic_arrow_select_light_blue
            ),
            contentDescription = null,
            tint = colorResource(R.color.blue200),
            modifier = if (hasAccess) Modifier.size(18.dp) else Modifier
        )
    }
}

@Composable
private fun NoAccountInviteSection(email: String, onInviteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.share_management_no_account_title),
            style = TextStyle(
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue900),
                letterSpacing = (-0.48).sp,
            )
        )

        val message = stringResource(R.string.share_management_no_account_message, email)
        val mediumFamily = FontFamily(Font(R.font.usual_medium))
        val annotatedMessage = remember(message, email) {
            buildAnnotatedString {
                append(message)
                val startIndex = message.indexOf(email)
                if (startIndex >= 0) {
                    addStyle(
                        style = SpanStyle(fontFamily = mediumFamily),
                        start = startIndex,
                        end = startIndex + email.length
                    )
                }
            }
        }
        Text(
            text = annotatedMessage,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue900),
            )
        )

        CenteredTextAndIconButton(
            buttonColor = ButtonColor.DARK,
            text = stringResource(R.string.invite_now),
            icon = painterResource(id = R.drawable.ic_send_paper_plane_white),
            iconAlignment = ButtonIconAlignment.END,
            iconSize = 24.dp,
            onButtonClick = onInviteClick
        )
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue400),
            )
        )
    }
}

@Composable
private fun PastSharesRow(onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            thickness = 1.dp,
            color = colorResource(R.color.blue50)
        )

        GrantAccessEntryRow(
            iconResId = R.drawable.ic_archives_blue,
            text = stringResource(R.string.select_an_archive_from_past_shares),
            onClick = onClick,
            modifier = Modifier.padding(all = 24.dp)
        )
    }
}
