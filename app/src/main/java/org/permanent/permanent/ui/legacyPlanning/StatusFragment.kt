package org.permanent.permanent.ui.legacyPlanning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.models.Archive
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.archives.PARCELABLE_ARCHIVE_KEY
import org.permanent.permanent.ui.legacyPlanning.compose.StatusScreen
import org.permanent.permanent.viewmodels.LegacyStatusViewModel

class StatusFragment : PermanentBaseFragment()  {
    private lateinit var viewModel: LegacyStatusViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(this)[LegacyStatusViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    StatusScreen(viewModel = viewModel,
                        navigateToArchiveStewardScreen = { archive: Archive ->
                            val bundle = bundleOf(PARCELABLE_ARCHIVE_KEY to archive)
                            findNavController().navigate(R.id.action_statusFragment_to_archiveStewardFragment, bundle)
                        },
                        navigateToLegacyContactScreen = {
                            findNavController().navigate(R.id.action_statusFragment_to_legacyContactFragment)
                        })
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

        viewModel.fetchData()
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}