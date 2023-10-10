package org.permanent.permanent.ui.compose.legacyPlanning

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import org.permanent.permanent.R
import org.permanent.permanent.ui.compose.legacyPlanning.DesignateContactOrStewardScreen
import org.permanent.permanent.viewmodels.LegacyContactViewModel

@Composable
fun LegacyContactScreen(viewModel: LegacyContactViewModel,
                        openAddEditScreen: () -> Unit,
                        openLegacyScreen: () -> Unit) {

    val userName = viewModel.contactName.observeAsState()
    val userEmail = viewModel.contactEmail.observeAsState()

    Column {
        DesignateContactOrStewardScreen(
            name = userName,
            email = userEmail,
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