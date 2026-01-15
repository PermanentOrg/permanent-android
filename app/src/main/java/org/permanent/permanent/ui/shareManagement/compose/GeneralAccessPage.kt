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
import org.permanent.permanent.ui.composeComponents.LinkSettingsMenuItem
import org.permanent.permanent.viewmodels.ShareManagementViewModel

@Composable
fun GeneralAccessPage(
    viewModel: ShareManagementViewModel,
    onClose: () -> Unit,
) {
    val selectedAccessType by viewModel.selectedGeneralAccessType.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top
        ) {

            NavigationHeader(
                title = stringResource(R.string.general_access),
                onBackBtnClick = { viewModel.onBackBtnClick(SharePage.GENERAL_ACCESS) },
                onCloseClick = onClose
            )

            val isAnyoneSelected = selectedAccessType == AccessType.ANYONE_CAN_VIEW
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_globe_green),
                title = stringResource(R.string.anyone_can_view),
                subtitle = stringResource(R.string.anyone_can_view_description),
                isSelected = isAnyoneSelected
            ) { viewModel.onGeneralAccessItemClick(AccessType.ANYONE_CAN_VIEW) }

            HorizontalDivider(
                thickness = 1.dp, color = colorResource(R.color.blue25)
            )

            val isRestrictedSelected = selectedAccessType == AccessType.RESTRICTED
            LinkSettingsMenuItem(
                iconResource = painterResource(id = R.drawable.ic_lock_green),
                title = stringResource(R.string.restricted),
                subtitle = stringResource(R.string.restricted_description),
                isSelected = isRestrictedSelected
            ) { viewModel.onGeneralAccessItemClick(AccessType.RESTRICTED) }

            HorizontalDivider(
                thickness = 1.dp, color = colorResource(R.color.blue25)
            )
        }
    }
}

enum class AccessType(val value: Int) {
    ANYONE_CAN_VIEW(0), RESTRICTED(1);

    val backendValue: String
        get() = when (this) {
            ANYONE_CAN_VIEW -> "none"
            RESTRICTED -> "approval"
        }

    companion object {
        fun fromBackendValue(value: String): AccessType? =
            entries.firstOrNull { it.backendValue == value }
    }
}