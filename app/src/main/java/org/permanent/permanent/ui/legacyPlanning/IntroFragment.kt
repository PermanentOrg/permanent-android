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
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.compose.legacyPlanning.IntroScreen
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
                    IntroScreen(navigateToDesignateContactScreen = {
                        findNavController().navigate(R.id.action_introFragment_to_legacyContactFragment)
                    }, onCloseScreen = {
                        findNavController().popBackStack(R.id.myFilesFragment, false)
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
    }

    override fun onPause() {
        super.onPause()
        disconnectViewModelEvents()
    }
}