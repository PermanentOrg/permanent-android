package org.permanent.permanent.ui.login

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.models.AccountEventAction
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.ui.login.compose.AuthPage
import org.permanent.permanent.ui.login.compose.AuthenticationContainer
import org.permanent.permanent.viewmodels.AuthenticationViewModel

const val START_DESTINATION_PAGE_VALUE_KEY = "start_destination_page_value_key"

class AuthenticationFragment : PermanentBaseFragment() {
    private lateinit var viewModel: AuthenticationViewModel
    private lateinit var prefsHelper: PreferencesHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
        viewModel.buildPromptParams(this)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extras = arguments
        val startDestPageValue = extras?.getInt(START_DESTINATION_PAGE_VALUE_KEY)
        startDestPageValue?.let {
            val targetPage = when (it) {
                AuthPage.SIGN_UP.value -> AuthPage.SIGN_UP
                AuthPage.BIOMETRICS.value -> {
                    if (!prefsHelper.isBiometricsLogIn() || viewModel.skipLogin()) {
                        navigateToMainActivity()
                        viewModel.sendEvent(AccountEventAction.LOGIN)
                        return
                    } else if (!prefsHelper.isUserLoggedIn()) {
                        onLoggedOut()
                        AuthPage.SIGN_IN
                    } else {
                        viewModel.authenticateUser()
                        AuthPage.BIOMETRICS
                    }
                }
                else -> AuthPage.SIGN_IN
            }
            viewModel.setNavigateToPage(targetPage)
        }
    }

    private fun showOpenSettingsQuestionDialog() {
        val activityContext = context as? Activity  // Safe cast to Activity

        if (activityContext != null && !activityContext.isFinishing && !activityContext.isDestroyed) {
            AlertDialog.Builder(activityContext).apply { // Use the Activity context
                setTitle(context.getString(R.string.login_biometric_error_no_biometrics_enrolled_title))
                setMessage(context.getString(R.string.login_biometric_error_no_biometrics_enrolled_message))
                setPositiveButton(R.string.yes_button) { _, _ ->
                    activityContext.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
                setNegativeButton(R.string.button_cancel) { _, _ -> }
                create()
                show()
            }
        } else {
            Log.w("AlertDialog", "Cannot show dialog: Activity context is invalid.")
        }
    }

    private fun onLoggedOut() {
        // Navigate to Sign in after this
    }

    private val onSignedIn = Observer<Void?> {
        navigateToMainActivity()
    }

    private val onAuthenticated = Observer<Void?> {
        navigateToMainActivity()
    }

    private val onAccountCreated = Observer<Void?> {
        startArchiveOnboardingActivity()
    }

    private val userMissingDefaultArchiveObserver = Observer<Void?> {
        startArchiveOnboardingActivity()
    }

    private val onShowSettingsDialog = Observer<Void?> {
        showOpenSettingsQuestionDialog()
    }

    private fun startArchiveOnboardingActivity() {
        startActivity(Intent(context, ArchiveOnboardingActivity::class.java))
        activity?.finish()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(context, MainActivity::class.java))
        activity?.finish()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnAccountCreated().observe(this, onAccountCreated)
        viewModel.getOnSignedIn().observe(this, onSignedIn)
        viewModel.getOnAuthenticated().observe(this, onAuthenticated)
        viewModel.getOnUserMissingDefaultArchive().observe(this, userMissingDefaultArchiveObserver)
        viewModel.getOnShowSettingsDialog().observe(this, onShowSettingsDialog)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnAccountCreated().removeObserver(onAccountCreated)
        viewModel.getOnSignedIn().removeObserver(onSignedIn)
        viewModel.getOnAuthenticated().removeObserver(onAuthenticated)
        viewModel.getOnUserMissingDefaultArchive().removeObserver(userMissingDefaultArchiveObserver)
        viewModel.getOnShowSettingsDialog().removeObserver(onShowSettingsDialog)
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