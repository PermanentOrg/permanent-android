package org.permanent.permanent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.settings.compose.twoStepVerification.TwoStepStatefulScreen
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

class TwoStepVerificationFragment : PermanentBaseFragment() {

    private val viewModel: LoginAndSecurityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    TwoStepStatefulScreen(viewModel)
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
        viewModel.clearSnackbar()
        disconnectViewModelEvents()
    }
}