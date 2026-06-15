package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Invitation
import org.permanent.permanent.models.Share
import org.permanent.permanent.ui.composeComponents.AccessRoleLabel
import org.permanent.permanent.ui.composeComponents.AccessRoleLabelColor
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
import org.permanent.permanent.ui.composeComponents.ConfirmationBottomSheet
import org.permanent.permanent.ui.composeComponents.OverlayColor
import org.permanent.permanent.ui.recordMenu.compose.RecordMenuHeader
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun ShareItemPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {

    val recordThumbURL by viewModel.recordThumb.collectAsState()
    val recordName by viewModel.recordName.collectAsState()
    val recordSize by viewModel.recordSize.collectAsState()
    val recordDate by viewModel.recordDate.collectAsState()
    val shareLink by viewModel.shareLink.collectAsState()
    val creatingLink by viewModel.isCreatingLinkState.collectAsState()
    val isLinkSharedState by viewModel.isLinkSharedState.collectAsState()
    val pendingShares by viewModel.pendingShares.collectAsState()
    val approvedShares by viewModel.approvedShares.collectAsState()
    val pendingInvites by viewModel.pendingInvites.collectAsState()
    val isApprovingAll by viewModel.isApprovingAll.collectAsState()
    val approvingShareIds by viewModel.approvingShareIds.collectAsState()
    var showDenyConfirmation by remember { mutableStateOf(false) }
    var selectedShare by remember { mutableStateOf<Share?>(null) }

    val showApproveAllFooter = pendingShares.size >= 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(colorResource(R.color.blue25))
            .wrapContentHeight(Alignment.Top)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            SheetHeader(
                title = "Share Item", onCloseClick = onClose
            )

            RecordMenuHeader(
                recordThumbURL = recordThumbURL,
                recordName = recordName,
                recordSize = recordSize,
                recordDate = recordDate,
                archiveThumb = "",
                archiveName = "",
                accessRole = AccessRole.VIEWER
            )

            if (creatingLink) {
                CreatingLinkRow()
            } else {
                if (isLinkSharedState) {
                    SharedLinkRow(shareLink = viewModel.cleanUrlRegex(shareLink), onSettingClick = {
                        viewModel.onLinkSettingsBtnClick()
                    }, onCopyClick = {
                        viewModel.copyLinkToClipboard()
                    })
                } else {
                    CreateLinkRow(onClick = {
                        viewModel.onCreateLinkBtnClick()
                    })
                }
            }

            GrantAccessToOtherArchivesSection(
                onFindByEmailClick = { viewModel.openFindArchiveByEmail() },
                onPastSharesClick = { viewModel.onPastSharesClick() }
            )

            ShareList(
                pendingShares = pendingShares,
                approvedShares = approvedShares,
                pendingInvites = pendingInvites,
                isApprovingAll = isApprovingAll,
                approvingShareIds = approvingShareIds,
                modifier = Modifier.weight(1f),
                contentBottomPadding = if (showApproveAllFooter) 88.dp else 32.dp,
                onEditClick = { share -> viewModel.onEditClick(share) },
                onApproveClick = { share -> viewModel.onApproveClick(share) },
                onDenyClick = { share ->
                    selectedShare = share
                    showDenyConfirmation = true
                },
                onEditInviteClick = { invitation -> viewModel.onEditInviteClick(invitation) }
            )
        }

        if (showApproveAllFooter) {
            ApproveAllFooter(
                enabled = !isApprovingAll,
                onClick = { viewModel.approveAllPendingShares() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showDenyConfirmation) {
        selectedShare?.archive?.fullName?.let {
            ConfirmationBottomSheet(
                message = stringResource(R.string.confirm_deny_access_message, it),
                boldText = it,
                confirmationButtonText = stringResource(id = R.string.deny_access),
                onConfirm = {
                    selectedShare?.let { share ->
                        viewModel.onDenyClick(share)
                    }
                    selectedShare = null
                    showDenyConfirmation = false
                },
                onDismiss = {
                    selectedShare = null
                    showDenyConfirmation = false
                }
            )
        }
    }
}

@Composable
fun ShareList(
    pendingShares: List<Share>,
    approvedShares: List<Share>,
    pendingInvites: List<Invitation>,
    isApprovingAll: Boolean,
    approvingShareIds: Set<Int>,
    modifier: Modifier = Modifier,
    contentBottomPadding: Dp = 32.dp,
    onEditClick: (Share) -> Unit,
    onApproveClick: (Share) -> Unit,
    onDenyClick: (Share) -> Unit,
    onEditInviteClick: (Invitation) -> Unit
) {
    if (pendingShares.isEmpty() && approvedShares.isEmpty() && pendingInvites.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(colorResource(R.color.white))
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(start = 24.dp, end = 16.dp, top = 24.dp),
        contentPadding = PaddingValues(bottom = contentBottomPadding)
    ) {

        item {
            ShareListTitle()
        }

        items(
            pendingShares, key = { it.id ?: it.hashCode() }) { share ->
            val isApprovingThis = share.id != null && share.id in approvingShareIds
            PendingShareItem(
                share = share,
                isApprovingThis = isApprovingThis,
                rowActionsEnabled = !isApprovingAll,
                onApproveClick = { onApproveClick(share) },
                onDenyClick = { onDenyClick(share) }
            )
        }

        items(
            approvedShares, key = { it.id ?: it.hashCode() }) { share ->
            ApprovedShareItem(share) { onEditClick(share) }
        }

        items(
            pendingInvites, key = { it.inviteId ?: it.hashCode() }) { invitation ->
            PendingInviteItem(invitation) { onEditInviteClick(invitation) }
        }
    }
}

@Composable
fun ShareListTitle() {
    Text(
        text = stringResource(R.string.current_requests_and_access).toUpperCase(Locale.current),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 8.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            color = colorResource(R.color.blue900),
            letterSpacing = 1.6.sp,
        )
    )
}

@Composable
fun GrantAccessToOtherArchivesSection(
    onFindByEmailClick: () -> Unit,
    onPastSharesClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(start = 24.dp, end = 24.dp, top = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.grant_access_to_other_archives)
                .toUpperCase(Locale.current),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 8.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue900),
                letterSpacing = 1.6.sp,
            )
        )

        GrantAccessEntryRow(
            iconResId = R.drawable.ic_search_middle_grey,
            text = stringResource(R.string.find_an_archive_using_email_address),
            onClick = onFindByEmailClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        GrantAccessEntryRow(
            iconResId = R.drawable.ic_archives_blue,
            text = stringResource(R.string.select_an_archive_from_past_shares),
            onClick = onPastSharesClick
        )
    }
}

@Composable
fun ApprovedShareItem(share: Share, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumb
        val thumbURL = share.archive?.thumbnail256 ?: share.archive?.thumbURL200
        if (thumbURL?.isNotEmpty() == true) {
            AsyncImage(
                model = thumbURL,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_archive_placeholder_blue),
                contentDescription = null,
                tint = Color.Unspecified
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
                text = share.archive?.fullName ?: "", style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            share.accessRole?.let {
                AccessRoleLabel(
                    accessRole = it,
                    fontSize = 8.sp,
                    lineHeight = 16.sp,
                    cornerSize = 4.dp,
                    color = AccessRoleLabelColor.LIGHT_BLUE
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Edit Icon
        IconButton(onClick = { onEditClick() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_primary),
                contentDescription = "Close",
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun PendingInviteItem(invitation: Invitation, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Gray archive-switcher placeholder: the invitee has no archive yet.
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(colorResource(R.color.blue100))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .size(width = 16.dp, height = 2.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = invitation.fullName ?: invitation.email ?: "",
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_medium)),
                        color = colorResource(R.color.blue900),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.invited),
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        color = colorResource(R.color.success500),
                    )
                )
            }

            Text(
                text = invitation.email ?: "",
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue600),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = { onEditClick() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_primary),
                contentDescription = "Edit invitation",
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun PendingShareItem(
    share: Share,
    isApprovingThis: Boolean = false,
    rowActionsEnabled: Boolean = true,
    onApproveClick: () -> Unit,
    onDenyClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_archive_placeholder_blue),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = share.archive?.fullName ?: "", style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.pending),
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.warning500),
                )
            )
        }

        if (isApprovingThis) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    overlayColor = OverlayColor.LIGHT,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            IconButton(
                onClick = onApproveClick,
                enabled = rowActionsEnabled,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_done_white),
                    tint = colorResource(R.color.success500),
                    contentDescription = "Approve"
                )
            }
        }

        IconButton(onClick = onDenyClick, enabled = rowActionsEnabled) {
            Icon(
                painter = painterResource(R.drawable.ic_deny),
                tint = Color.Unspecified,
                contentDescription = "Deny"
            )
        }
    }
}

@Composable
fun ApproveAllFooter(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val whiteColor = colorResource(R.color.white)
    val successColor = colorResource(R.color.success500)
    val fadeBrush = remember(whiteColor) {
        Brush.verticalGradient(colors = listOf(Color.Transparent, whiteColor))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(brush = fadeBrush)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteColor)
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = successColor,
                    contentColor = whiteColor,
                    disabledContainerColor = successColor.copy(alpha = 0.5f),
                    disabledContentColor = whiteColor
                )
            ) {
                Text(
                    text = stringResource(R.string.approve_all),
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_medium)),
                        color = whiteColor
                    )
                )
            }
        }
    }
}

@Composable
fun CreatingLinkRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(start = 36.dp, top = 24.dp, bottom = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            overlayColor = OverlayColor.LIGHT, modifier = Modifier
                .height(16.dp)
                .width(16.dp)
        )

        Spacer(modifier = Modifier.width(28.dp))

        Text(
            text = stringResource(R.string.creating_link), style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                brush = Brush.linearGradient(
                    colors = listOf(
                        colorResource(R.color.barneyPurple), colorResource(R.color.colorAccent)
                    ), start = Offset(0f, 0f), end = Offset(300f, 300f)
                ),
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SharedLinkRow(
    shareLink: String, onSettingClick: () -> Unit, onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .padding(start = 24.dp, top = 6.dp, bottom = 24.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.blue50),
                    shape = RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(R.color.white))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock_closed),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = shareLink, modifier = Modifier.weight(1f), style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.usual_regular)),
                        textAlign = TextAlign.Left,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorResource(R.color.barneyPurple),
                                colorResource(R.color.colorAccent)
                            ), start = Offset(0f, 0f), end = Offset(300f, 300f)
                        ),
                    ), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onSettingClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = onCopyClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_copy_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}

@Composable
fun CreateLinkRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.blue25))
            .clickable { onClick() }
            .padding(start = 24.dp, top = 12.dp, bottom = 24.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {

        Icon(
            painter = painterResource(id = R.drawable.ic_share_link),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .padding(top = 4.dp, end = 24.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = stringResource(R.string.create_link_to_share), style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.create_link_description), style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.blue400),
                ), maxLines = 2, overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SheetHeader(
    title: String, onCloseClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.white))
                .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title, style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_medium)),
                    color = colorResource(R.color.blue900),
                    textAlign = TextAlign.Center
                ), maxLines = 1, overflow = TextOverflow.Ellipsis
            )

            if (onCloseClick != null) {
                IconButton(
                    onClick = onCloseClick, modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close_light_blue),
                        contentDescription = "Close",
                        tint = colorResource(R.color.blue200),
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 1.dp, color = colorResource(R.color.blue50)
        )
    }
}

enum class LinkState {
    SHARED, LOADING, NOTSHARED
}
