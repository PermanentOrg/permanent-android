package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.ui.composeComponents.ConfirmationBottomSheet
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun EditInvitationPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val invitation by viewModel.editingInvitation.collectAsState()
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
                title = stringResource(R.string.edit_invitation),
                onBackBtnClick = { viewModel.onBackBtnClick(SharePage.EDIT_INVITATION) },
                onCloseClick = onClose
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp)
            ) {

                InviteSectionLabel(stringResource(R.string.recipient_email_address))

                Spacer(modifier = Modifier.height(16.dp))

                RecipientField(text = invitation?.email ?: "")

                Spacer(modifier = Modifier.height(24.dp))

                InviteSectionLabel(stringResource(R.string.recipient_full_name))

                Spacer(modifier = Modifier.height(16.dp))

                RecipientField(text = invitation?.fullName ?: "")

                Spacer(modifier = Modifier.height(24.dp))

                InviteSectionLabel(stringResource(R.string.access_role))

                Spacer(modifier = Modifier.height(24.dp))

                // Read-only: the backend has no endpoint for updating a pending
                // invitation's access role yet.
                InviteAccessRoleRow(accessRole = invitation?.accessRole ?: AccessRole.VIEWER)

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(thickness = 1.dp, color = colorResource(R.color.blue50))

                Spacer(modifier = Modifier.height(24.dp))

                InviteActionRow(
                    iconResId = R.drawable.ic_arrow_rotate_right_primary,
                    text = stringResource(R.string.send_again),
                    color = colorResource(R.color.blue900),
                    onClick = { viewModel.onResendInviteClick() }
                )

                Spacer(modifier = Modifier.height(24.dp))

                InviteActionRow(
                    iconResId = R.drawable.ic_delete_red,
                    text = stringResource(R.string.revoke_invitation),
                    color = colorResource(R.color.error500),
                    onClick = { showRevokeConfirmation = true }
                )

                Spacer(modifier = Modifier.weight(1f))

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
                            onButtonClick = { viewModel.onBackBtnClick(SharePage.EDIT_INVITATION) })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        CenteredTextAndIconButton(
                            buttonColor = ButtonColor.DARK,
                            text = stringResource(id = R.string.done),
                            icon = null,
                            onButtonClick = { viewModel.onBackBtnClick(SharePage.EDIT_INVITATION) })
                    }
                }
            }
        }
    }

    if (showRevokeConfirmation) {
        val recipient = invitation?.fullName ?: invitation?.email ?: ""
        ConfirmationBottomSheet(
            message = stringResource(R.string.confirm_revoke_invitation, recipient),
            boldText = recipient,
            confirmationButtonText = stringResource(id = R.string.revoke_invitation),
            onConfirm = {
                showRevokeConfirmation = false
                viewModel.onRevokeInviteConfirmed()
            },
            onDismiss = { showRevokeConfirmation = false }
        )
    }
}

@Composable
private fun InviteActionRow(
    iconResId: Int,
    text: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text, modifier = Modifier.weight(1f), style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = color,
            )
        )
    }
}
