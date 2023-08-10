package org.permanent.permanent.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.permanent.permanent.R

@Composable

fun LegacyContactScreen(openAddEditScreen: () -> Unit) {

    Column {
        DesignateContactOrStewardScreen(
            title = stringResource(R.string.designate_a_legacy_contact),
            subtitle = stringResource(R.string.designate_contact_title),
            cardTitle = stringResource(R.string.a_trusted_legacy_contact_title),
            cardSubtitle = stringResource(R.string.a_trusted_legacy_contact_description),
            cardButtonName = stringResource(R.string.add_legacy_contact),
            openAddEditScreen = openAddEditScreen
        )
    }
}