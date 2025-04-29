package org.permanent.permanent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.settings.compose.LoginAndSecurityMenu
import org.permanent.permanent.viewmodels.ChangePasswordViewModel
import org.permanent.permanent.viewmodels.LoginAndSecurityViewModel

class LoginAndSecurityFragment : PermanentBaseFragment() {

    private val loginAndSecurityViewModel: LoginAndSecurityViewModel by activityViewModels()
    private val changePasswordViewModel: ChangePasswordViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    LoginAndSecurityMenu(
                        loginAndSecurityViewModel = loginAndSecurityViewModel,
                        changePasswordViewModel = changePasswordViewModel,
                        onChangePasswordClick = { findNavController().navigate(R.id.action_loginAndSecurityFragment_to_changePasswordFragment) },
                        onTwoStepVerificationClick = { findNavController().navigate(R.id.action_loginAndSecurityFragment_to_twoStepVerificationFragment) }
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