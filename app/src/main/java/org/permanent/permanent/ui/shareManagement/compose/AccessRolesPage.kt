package org.permanent.permanent.ui.shareManagement.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.permanent.permanent.R
import org.permanent.permanent.models.AccessRole
import org.permanent.permanent.ui.composeComponents.LinkSettingsMenuItem
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun AccessRolesPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val selectedAccessRole by viewModel.selectedAccessRole.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.select_access_role),
                onBackBtnClick = { viewModel.onBackBtnClick(SharePage.ACCESS_ROLES) },
                onCloseClick = onClose
            )

            val isOwnerSelected = selectedAccessRole == AccessRole.OWNER
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_owner_green),
                title = AccessRole.OWNER.toTitleCase(),
                subtitle = stringResource(R.string.owner_description),
                isSelected = isOwnerSelected
            ) { viewModel.onAccessRoleClick(AccessRole.OWNER) }

            HorizontalDivider(
                thickness = 1.dp, color = colorResource(R.color.blue25)
            )

            val isCuratorSelected = selectedAccessRole == AccessRole.CURATOR
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_curator_green),
                title = AccessRole.CURATOR.toTitleCase(),
                subtitle = stringResource(R.string.curator_description),
                isSelected = isCuratorSelected
            ) { viewModel.onAccessRoleClick(AccessRole.CURATOR) }

            HorizontalDivider(
                thickness = 1.dp, color = colorResource(R.color.blue25)
            )

            val isEditorSelected = selectedAccessRole == AccessRole.EDITOR
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_editor_green),
                title = AccessRole.EDITOR.toTitleCase(),
                subtitle = stringResource(R.string.editor_description),
                isSelected = isEditorSelected
            ) { viewModel.onAccessRoleClick(AccessRole.EDITOR) }

            HorizontalDivider(
                thickness = 1.dp, color = colorResource(R.color.blue25)
            )

            val isContributorSelected = selectedAccessRole == AccessRole.CONTRIBUTOR
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_contributor_green),
                title = AccessRole.CONTRIBUTOR.toTitleCase(),
                subtitle = stringResource(R.string.contributor_description),
                isSelected = isContributorSelected
            ) { viewModel.onAccessRoleClick(AccessRole.CONTRIBUTOR) }

            HorizontalDivider(
                thickness = 1.dp, color = colorResource(R.color.blue25)
            )

            val isViewerSelected = selectedAccessRole == AccessRole.VIEWER
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_viewer_green),
                title = AccessRole.VIEWER.toTitleCase(),
                subtitle = stringResource(R.string.viewer_description),
                isSelected = isViewerSelected
            ) { viewModel.onAccessRoleClick(AccessRole.VIEWER) }
        }
    }
}