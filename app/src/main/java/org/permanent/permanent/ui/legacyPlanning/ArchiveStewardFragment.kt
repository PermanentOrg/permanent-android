package org.permanent.permanent.ui.legacyPlanning

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archives.PARCELABLE_ARCHIVE_KEY
import org.permanent.permanent.ui.compose.ArchiveStewardScreen
import org.permanent.permanent.ui.compose.LegacyContactScreen

class ArchiveStewardFragment : PermanentBaseFragment() {

    private var archive: Archive? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            archive = arguments?.getParcelable(PARCELABLE_ARCHIVE_KEY)
            setContent {
                MaterialTheme {
                    ArchiveStewardScreen(archive = archive)
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