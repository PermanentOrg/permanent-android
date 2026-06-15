package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun InviteAndGrantAccessPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val email by viewModel.inviteEmail.collectAsState()
    val fullName by viewModel.inviteFullName.collectAsState()
    val accessRole by viewModel.inviteAccessRole.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.invite_and_grant_access),
                onBackBtnClick = {
                    keyboardController?.hide()
                    viewModel.onBackBtnClick(SharePage.INVITE_ACCESS)
                },
                onCloseClick = {
                    keyboardController?.hide()
                    onClose()
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp)
            ) {

                InviteSectionLabel(stringResource(R.string.recipient_email_address))

                Spacer(modifier = Modifier.height(16.dp))

                RecipientField(text = email)

                Spacer(modifier = Modifier.height(24.dp))

                InviteSectionLabel(stringResource(R.string.recipient_full_name))

                Spacer(modifier = Modifier.height(16.dp))

                RecipientNameField(
                    value = fullName,
                    onValueChange = { viewModel.onInviteFullNameChange(it) })

                Spacer(modifier = Modifier.height(24.dp))

                InviteSectionLabel(stringResource(R.string.access_role))

                Spacer(modifier = Modifier.height(24.dp))

                InviteAccessRoleRow(
                    accessRole = accessRole,
                    onClick = {
                        keyboardController?.hide()
                        viewModel.onInviteAccessRoleClick()
                    })

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
                            onButtonClick = {
                                keyboardController?.hide()
                                viewModel.onBackBtnClick(SharePage.INVITE_ACCESS)
                            })
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        CenteredTextAndIconButton(
                            buttonColor = ButtonColor.DARK,
                            text = stringResource(id = R.string.send_invitation),
                            icon = null,
                            onButtonClick = {
                                keyboardController?.hide()
                                viewModel.onConfirmSendInvite()
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun InviteSectionLabel(text: String) {
    Text(
        text = text.toUpperCase(Locale.current),
        style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            color = colorResource(R.color.colorPrimary),
            letterSpacing = 1.6.sp,
        )
    )
}

@Composable
private fun RecipientNameField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.blue100),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue900),
            ),
            cursorBrush = SolidColor(colorResource(R.color.colorPrimary)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.recipient_full_name),
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
}

@Composable
fun RecipientField(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(colorResource(R.color.blue25), RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.blue50),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_regular)),
                color = colorResource(R.color.blue900),
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun InviteAccessRoleRow(
    accessRole: AccessRole,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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
                painter = painterResource(id = accessRole.iconRes()),
                contentDescription = "", tint = colorResource(R.color.blue900)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = accessRole.toTitleCase(), modifier = Modifier.weight(1f), style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.colorPrimary),
            )
        )

        if (onClick != null) {
            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                modifier = Modifier.padding(end = 8.dp),
                painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
                contentDescription = null,
                tint = colorResource(R.color.blue200)
            )
        }
    }
}
