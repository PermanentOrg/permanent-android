package org.permanent.permanent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.settings.compose.TwoStepVerificationStatefulScreen
import org.permanent.permanent.viewmodels.TwoStepVerificationViewModel

class TwoStepVerificationFragment : PermanentBaseFragment() {

    private lateinit var viewModel: TwoStepVerificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[TwoStepVerificationViewModel::class.java]

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TwoStepVerificationStatefulScreen(viewModel)
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