package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Share
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.ConfirmationBottomSheet
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun ArchiveAccessPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val share by viewModel.editingShare.collectAsState()
    val accessRole by viewModel.editingArchiveAccessRole.collectAsState()
    var showRevokeConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.edit_archive_access),
                onBackBtnClick = {
                    viewModel.clearEditingShare()
                    viewModel.onBackBtnClick(SharePage.ARCHIVE_ACCESS)
                },
                onCloseClick = {
                    viewModel.clearEditingShare()
                    onClose()
                })

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp)
            ) {

                GrantAccessToArchive(share)

                Spacer(modifier = Modifier.height(24.dp))

                AccessRole(accessRole, viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.blue50))

                Spacer(modifier = Modifier.height(24.dp))

                RevokeAccess(onClick = { showRevokeConfirmation = true })

                Spacer(modifier = Modifier.weight(1f))

                ButtonsRow(viewModel)
            }
        }
    }

    if (showRevokeConfirmation) {
        share?.archive?.fullName?.let {
            ConfirmationBottomSheet(
                message = stringResource(R.string.confirm_revoke_access_message, it),
                boldText = it,
                confirmationButtonText = stringResource(id = R.string.revoke_access),
                onConfirm = {
                    showRevokeConfirmation = false
                    viewModel.revokeAccess(share!!)
                },
                onDismiss = {
                    showRevokeConfirmation = false
                })
        }
    }
}

@Composable
private fun GrantAccessToArchive(share: Share?) {
    Text(
        text = stringResource(R.string.grant_access_to_archive).toUpperCase(Locale.current),
        style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            color = colorResource(R.color.colorPrimary),
            letterSpacing = 1.6.sp,
        )
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumb
        val thumbURL = share?.archive?.thumbURL200
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

        Text(
            text = share?.archive?.fullName ?: "", style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.blue900),
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AccessRole(accessRole: AccessRole, viewModel: ShareManagementViewModel) {
    Text(
        text = stringResource(R.string.access_role).toUpperCase(Locale.current), style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            color = colorResource(R.color.colorPrimary),
            letterSpacing = 1.6.sp,
        )
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.onArchiveAccessRoleClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    colorResource(R.color.blue25), RoundedCornerShape(4.dp)
                ), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = when (accessRole) {
                        AccessRole.VIEWER -> R.drawable.ic_viewer_green
                        AccessRole.CONTRIBUTOR -> R.drawable.ic_contributor_green
                        AccessRole.EDITOR -> R.drawable.ic_editor_green
                        AccessRole.CURATOR -> R.drawable.ic_curator_green
                        AccessRole.OWNER -> R.drawable.ic_owner_green
                        AccessRole.MANAGER -> TODO()
                    }
                ), contentDescription = "", tint = colorResource(R.color.blue900)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = when (accessRole) {
                AccessRole.VIEWER -> AccessRole.VIEWER.toTitleCase()
                AccessRole.CONTRIBUTOR -> AccessRole.CONTRIBUTOR.toTitleCase()
                AccessRole.EDITOR -> AccessRole.EDITOR.toTitleCase()
                AccessRole.CURATOR -> AccessRole.CURATOR.toTitleCase()
                AccessRole.OWNER -> AccessRole.OWNER.toTitleCase()
                AccessRole.MANAGER -> AccessRole.MANAGER.toTitleCase()
            }, modifier = Modifier.weight(1f), style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.colorPrimary),
            )
        )

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            modifier = Modifier.padding(end = 8.dp),
            painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
            contentDescription = "Close",
            tint = colorResource(R.color.blue200)
        )
    }
}

@Composable
private fun RevokeAccess(onClick: () -> Unit) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_delete_red),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = stringResource(R.string.revoke_access), style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.usual_regular)),
                    color = colorResource(R.color.error500)
                )
            )
        }
    }
}

@Composable
private fun ButtonsRow(viewModel: ShareManagementViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.LIGHT_BLUE,
                text = stringResource(id = R.string.button_cancel),
                icon = null,
                onButtonClick = { viewModel.onBackBtnClick(SharePage.ARCHIVE_ACCESS) })
        }

        Box(modifier = Modifier.weight(1f)) {
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.save),
                icon = null,
                onButtonClick = { viewModel.onSaveBtnClick() })
        }
    }
}
