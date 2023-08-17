package org.permanent.permanent.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import org.permanent.permanent.R
import org.permanent.permanent.viewmodels.LegacyContactViewModel

@Composable

fun LegacyContactScreen(viewModel: LegacyContactViewModel,
                        openAddEditScreen: () -> Unit,
                        openLegacyScreen: () -> Unit) {

    val legacyStewards = viewModel.getOnLegacyContactReady().observeAsState()
    val name = if(legacyStewards.value?.isEmpty() == true) null else legacyStewards.value?.first()?.name
    val email = if(legacyStewards.value?.isEmpty() == true) null else legacyStewards.value?.first()?.email

    Column {
        DesignateContactOrStewardScreen(
            name = name,
            email = email,
            title = stringResource(R.string.designate_a_legacy_contact),
            subtitle = stringResource(R.string.designate_contact_title),
            cardTitle = stringResource(R.string.a_trusted_legacy_contact_title),
            cardSubtitle = stringResource(R.string.a_trusted_legacy_contact_description),
            cardButtonName = stringResource(R.string.add_legacy_contact),
            openAddEditScreen = openAddEditScreen,
            openLegacyScreen = openLegacyScreen
        )
    }
}