package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBottomSheetFragment
import org.permanent.permanent.ui.compose.AddEditLegacyContactScreen

class AddEditLegacyContactFragment : PermanentBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AddEditLegacyContactScreen(
                        screenTitle = stringResource(R.string.legacy_contact),
                        title = stringResource(R.string.designate_account_legacy_contact),
                        subtitle = stringResource(R.string.designate_account_legacy_contact_description),
                        namePlaceholder = stringResource(R.string.contact_name),
                        emailPlaceholder = stringResource(R.string.contact_email_address),
                        note = stringResource(R.string.note_description)
                    )
                }
            }
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }

    override fun onResume() {
        super.onResume()
        connectViewModelEvents()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}