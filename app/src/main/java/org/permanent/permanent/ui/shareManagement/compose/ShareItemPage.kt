package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Share
import org.permanent.permanent.ui.composeComponents.AccessRoleLabel
import org.permanent.permanent.ui.composeComponents.AccessRoleLabelColor
import org.permanent.permanent.ui.composeComponents.CircularProgressIndicator
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

            ShareList(
                pendingShares = pendingShares,
                approvedShares = approvedShares,
                modifier = Modifier.weight(1f),
                onEditClick = { share -> viewModel.onEditClick(share) })
        }
    }
}

@Composable
fun ShareList(
    pendingShares: List<Share>,
    approvedShares: List<Share>,
    modifier: Modifier = Modifier,
    onEditClick: (Share) -> Unit
) {
    if (pendingShares.isEmpty() && approvedShares.isEmpty()) return

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(start = 24.dp, end = 16.dp, top = 32.dp, bottom = 32.dp)
    ) {

        item {
            ShareListTitle()
        }

        items(
            approvedShares, key = { it.id ?: it.hashCode() }) { share ->
            ApprovedShareItem(share) { onEditClick(share) }
        }

        items(
            pendingShares, key = { it.id ?: it.hashCode() }) { share ->
            PendingShareItem(share)
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
fun PendingShareItem(share: Share) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.white))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = share.archive?.fullName ?: "",
                fontSize = 14.sp,
                color = colorResource(R.color.blue900)
            )
            Text(
                text = "pending", fontSize = 12.sp, color = colorResource(R.color.blue400)
            )
        }

        IconButton(onClick = { /* approve */ }) {
            Icon(
                painter = painterResource(R.drawable.ic_done_white),
                tint = colorResource(R.color.green),
                contentDescription = null
            )
        }

        IconButton(onClick = { /* deny */ }) {
            Icon(
                painter = painterResource(R.drawable.ic_close_white),
                tint = colorResource(R.color.green),
                contentDescription = null
            )
        }
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
        val thumbURL = share.archive?.thumbURL200
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
                painter = painterResource(id = R.drawable.ic_archive_placeholder_multicolor),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(6.dp))
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
                AccessRoleLabel(accessRole = it, fontSize = 8.sp, lineHeight = 16.sp, cornerSize = 4.dp, color = AccessRoleLabelColor.LIGHT_BLUE)
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
            .padding(start = 24.dp, top = 6.dp, bottom = 6.dp, end = 24.dp),
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
            .padding(start = 24.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
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
