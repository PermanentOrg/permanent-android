package org.permanent.permanent.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.EventType
import org.permanent.permanent.EventsManager
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.ui.login.compose.AuthenticationContainer
import org.permanent.permanent.viewmodels.AuthenticationViewModel

class AuthenticationFragment : PermanentBaseFragment() {
    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]

        prefsHelper = PreferencesHelper(
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    AuthenticationContainer(viewModel)
                }
            }
        }
    }

    private val onLoggedIn = Observer<Void?> {
        logEvents()
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    private val onAccountCreated = Observer<Void?> {
        logEvents()
        startActivity(Intent(context, ArchiveOnboardingActivity::class.java))
        activity?.finish()
    }

    private val userMissingDefaultArchiveObserver = Observer<Void?> {
        startActivity(Intent(context, ArchiveOnboardingActivity::class.java))
        activity?.finish()
    }

    private fun logEvents() {
        EventsManager(requireContext()).setUserProfile(
            prefsHelper.getAccountId(), prefsHelper.getAccountEmail()
        )
        EventsManager(requireContext()).sendToMixpanel(EventType.SignIn)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnAccountCreated().observe(this, onAccountCreated)
        viewModel.getOnLoggedIn().observe(this, onLoggedIn)
        viewModel.getOnUserMissingDefaultArchive().observe(this, userMissingDefaultArchiveObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnAccountCreated().removeObserver(onAccountCreated)
        viewModel.getOnLoggedIn().removeObserver(onLoggedIn)
        viewModel.getOnUserMissingDefaultArchive().removeObserver(userMissingDefaultArchiveObserver)
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