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
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.compose.IntroScreen
import org.permanent.permanent.viewmodels.IntroViewModel

class IntroFragment : PermanentBaseFragment() {
    private lateinit var viewModel: IntroViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[IntroViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    IntroScreen(onCloseScreen = {
                        findNavController().navigateUp()
                        findNavController().navigateUp()
                    })
                }
            }
        }
    }

    private val onLegacyContactObserver = Observer<Void?> {
        findNavController().navigate(R.id.action_loadingFragment_to_introFragment)
    }

    override fun connectViewModelEvents() {
//        viewModel.getOnLegacyContactReady().observe(this, onLegacyContactObserver)
    }

    override fun disconnectViewModelEvents() {
//        viewModel.getOnLegacyContactReady().removeObserver(onLegacyContactObserver)
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