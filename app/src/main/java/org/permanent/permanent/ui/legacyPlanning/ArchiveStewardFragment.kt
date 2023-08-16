package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.archives.PARCELABLE_ARCHIVE_KEY
import org.permanent.permanent.ui.compose.ArchiveStewardScreen
import org.permanent.permanent.viewmodels.ArchiveStewardViewModel
import org.permanent.permanent.viewmodels.LegacyContactViewModel

class ArchiveStewardFragment : PermanentBaseFragment() {

    private var archive: Archive? = null
    private var addEditArchiveStewardFragment: AddEditArchiveStewardFragment? = null
    private lateinit var viewModel: ArchiveStewardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        archive = arguments?.getParcelable(PARCELABLE_ARCHIVE_KEY)

        viewModel = ViewModelProvider(this)[ArchiveStewardViewModel::class.java]
        viewModel.archive = archive

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ArchiveStewardScreen(
                        viewModel = viewModel,
                        archive = archive,
                        openAddEditScreen = {
                        addEditArchiveStewardFragment = AddEditArchiveStewardFragment()
                        addEditArchiveStewardFragment?.show(parentFragmentManager, addEditArchiveStewardFragment?.tag) },
                        openLegacyScreen = {
                            findNavController().navigate(R.id.action_archiveStewardFragment_to_statusFragment)
                        })
                }
            }
        }
    }

    override fun connectViewModelEvents() {
        archive?.id?.let {
            viewModel.getArchiveSteward(it)
        }
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