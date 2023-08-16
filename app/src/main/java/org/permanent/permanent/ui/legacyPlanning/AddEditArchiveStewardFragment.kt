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

class AddEditArchiveStewardFragment : PermanentBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AddEditLegacyContactScreen(
                        screenTitle = stringResource(R.string.archive_steward),
                        title = stringResource(R.string.designate_archive_steward),
                        subtitle = stringResource(R.string.designate_archive_steward_description),
                        namePlaceholder = stringResource(R.string.steward_name),
                        emailPlaceholder = stringResource(R.string.steward_email_address),
                        note = stringResource(R.string.steward_note_description)
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