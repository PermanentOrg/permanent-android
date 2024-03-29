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
import org.permanent.permanent.network.models.LegacyContact
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.legacyPlanning.compose.LoadingScreen
import org.permanent.permanent.viewmodels.LoadingViewModel

class LoadingFragment : PermanentBaseFragment() {
    private lateinit var viewModel: LoadingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[LoadingViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LoadingScreen()
                }
            }
        }
    }

    private val onLegacyContactObserver = Observer<List<LegacyContact>?> {
        if (!it.isNullOrEmpty()) {
            findNavController().navigate(R.id.action_loadingFragment_to_statusFragment)
        } else {
            findNavController().navigate(R.id.action_loadingFragment_to_introFragment)
        }

    }

    override fun connectViewModelEvents() {
        viewModel.getOnLegacyContactReady().observe(this, onLegacyContactObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnLegacyContactReady().removeObserver(onLegacyContactObserver)
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