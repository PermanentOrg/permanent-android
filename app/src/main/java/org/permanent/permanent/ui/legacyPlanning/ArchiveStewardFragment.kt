package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.models.Archive
import org.permanent.permanent.network.models.ArchiveSteward
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.archives.PARCELABLE_ARCHIVE_KEY
import org.permanent.permanent.ui.legacyPlanning.compose.ArchiveStewardScreen
import org.permanent.permanent.viewmodels.ArchiveStewardViewModel

class ArchiveStewardFragment : PermanentBaseFragment() {

    private var archive: Archive? = null
    private var archiveSteward: ArchiveSteward? = null
    private var addEditArchiveStewardFragment: AddEditArchiveStewardFragment? = null
    private lateinit var viewModel: ArchiveStewardViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        archive = arguments?.getParcelable(PARCELABLE_ARCHIVE_KEY)

        viewModel = ViewModelProvider(this)[ArchiveStewardViewModel::class.java]
        viewModel.sendEvent(AccountEventAction.OPEN_ARCHIVE_STEWARD)
        viewModel.getLegacyContact()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    ArchiveStewardScreen(
                        viewModel = viewModel,
                        archive = archive,
                        openAddEditScreen = {
                            addEditArchiveStewardFragment = AddEditArchiveStewardFragment()
                            addEditArchiveStewardFragment?.show(parentFragmentManager, addEditArchiveStewardFragment?.tag)
                            addEditArchiveStewardFragment?.setBundleArguments(archive?.id, archiveSteward)
                            addEditArchiveStewardFragment?.getOnArchiveStewardUpdated()?.observe(this@ArchiveStewardFragment, onArchiveStewardUpdatedObserver) },
                        openLegacyScreen = {
                            findNavController().navigate(R.id.action_archiveStewardFragment_to_statusFragment)
                        },
                        openIntroScreen = {
                            findNavController().navigate(R.id.action_archiveStewardFragment_to_introFragment)
                        }
                    )
                }
            }
        }
    }

    private val onArchiveStewardReadyObserver = Observer<ArchiveSteward?> {
        archiveSteward = it
    }

    private val onArchiveStewardUpdatedObserver = Observer<ArchiveSteward> {
        archiveSteward = it
        viewModel.onArchiveStewardUpdated(it)
    }

    override fun connectViewModelEvents() {
        archive?.id?.let {
            viewModel.getArchiveSteward(it)
        }
        viewModel.getOnArchiveStewardReady().observe(this, onArchiveStewardReadyObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchiveStewardReady().removeObserver(onArchiveStewardReadyObserver)
        addEditArchiveStewardFragment?.getOnArchiveStewardUpdated()?.removeObserver(onArchiveStewardUpdatedObserver)
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