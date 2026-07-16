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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.composeComponents.ButtonColor
import org.permanent.permanent.ui.composeComponents.CenteredTextAndIconButton
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun GrantArchiveAccessPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val archive by viewModel.selectedArchiveForGrant.collectAsState()
    val accessRole by viewModel.grantAccessRole.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.grant_access),
                onBackBtnClick = { viewModel.onBackBtnClick(SharePage.GRANT_ARCHIVE_ACCESS) },
                onCloseClick = onClose
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 24.dp)
            ) {

                GrantAccessToArchive(archive)

                Spacer(modifier = Modifier.height(24.dp))

                GrantAccessRole(accessRole, viewModel)

                Spacer(modifier = Modifier.weight(1f))

                ButtonsRow(viewModel)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.toUpperCase(Locale.current),
        style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 10.sp,
            fontFamily = FontFamily(Font(R.font.usual_regular)),
            color = colorResource(R.color.colorPrimary),
            letterSpacing = 1.6.sp,
        )
    )
}

@Composable
private fun GrantAccessToArchive(archive: Archive?) {
    SectionLabel(stringResource(R.string.grant_access_to_archive))

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        ArchiveThumbnail(archive)

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = archive?.fullName ?: "", style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.usual_medium)),
                color = colorResource(R.color.blue900),
            ), maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GrantAccessRole(accessRole: AccessRole, viewModel: ShareManagementViewModel) {
    SectionLabel(stringResource(R.string.access_role))

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.onGrantAccessRoleClick() },
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

        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            modifier = Modifier.padding(end = 8.dp),
            painter = painterResource(id = R.drawable.ic_arrow_select_light_blue),
            contentDescription = null,
            tint = colorResource(R.color.blue200)
        )
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
                onButtonClick = { viewModel.onBackBtnClick(SharePage.GRANT_ARCHIVE_ACCESS) })
        }

        Box(modifier = Modifier.weight(1f)) {
            CenteredTextAndIconButton(
                buttonColor = ButtonColor.DARK,
                text = stringResource(id = R.string.grant_access),
                icon = null,
                onButtonClick = { viewModel.onConfirmGrantAccess() })
        }
    }
}
