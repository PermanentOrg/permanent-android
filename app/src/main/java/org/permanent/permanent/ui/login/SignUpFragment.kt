package org.permanent.permanent.ui.login

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.permanent.permanent.EventType
import org.permanent.permanent.EventsManager
import org.permanent.permanent.R
import org.permanent.permanent.ui.PermanentBaseFragment
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.archiveOnboarding.ArchiveOnboardingActivity
import org.permanent.permanent.viewmodels.SignUpViewModel

class SignUpFragment : PermanentBaseFragment() {
    private lateinit var viewModel: SignUpViewModel
    private lateinit var prefsHelper: PreferencesHelper

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
//        prefsHelper = PreferencesHelper(
//            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
//        )
//
//        return binding.root
//    }

    private val onReadyToShowTermsDialog = Observer<Void?> { showTermsDialog() }

    private val showLoginScreenObserver = Observer<Void?> {
        findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
    }

    private val onErrorMessage = Observer<String> { errorMessage ->
//        val snackBar = Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG)
//        val view: View = snackBar.view
//        context?.let {
//            view.setBackgroundColor(ContextCompat.getColor(it, R.color.deepRed))
//            snackBar.setTextColor(ContextCompat.getColor(it, R.color.white))
//        }
//        snackBar.show()
    }

    private val startArchiveOnboardingActivity = Observer<Void?> {
        logEvents()
        startActivity(Intent(context, ArchiveOnboardingActivity::class.java))
        activity?.finish()
    }

    private fun showTermsDialog() {
//        context?.hideKeyboardFrom(binding.root.windowToken)
//        val dialogBinding: DialogTermsOfServiceBinding = DataBindingUtil.inflate(
//            LayoutInflater.from(context), R.layout.dialog_terms_of_service, null, false
//        )
//        val alert = android.app.AlertDialog.Builder(context).setView(dialogBinding.root).create()
//
//        dialogBinding.webviewtermsOfService.loadUrl(BuildConfig.TERMS_URL)
//        dialogBinding.btnAccept.setOnClickListener {
//            viewModel.makeAccount()
//            alert.dismiss()
//        }
//        dialogBinding.btnDecline.setOnClickListener {
//            Toast.makeText(context, R.string.sign_up_terms_declined, Toast.LENGTH_SHORT).show()
//            alert.dismiss()
//        }
//        alert.show()
    }

    private fun logEvents() {
        EventsManager(requireContext()).setUserProfile(prefsHelper.getAccountId(), prefsHelper.getAccountEmail())
        EventsManager(requireContext()).sendToMixpanel(EventType.SignUp)
    }

    override fun connectViewModelEvents() {
        viewModel.getOnAccountCreated().observe(this, startArchiveOnboardingActivity)
        viewModel.getOnErrorMessage().observe(this, onErrorMessage)
        viewModel.getOnReadyToShowTermsDialog().observe(this, onReadyToShowTermsDialog)
        viewModel.getShowLoginScreen().observe(this, showLoginScreenObserver)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnAccountCreated().removeObserver(startArchiveOnboardingActivity)
        viewModel.getOnErrorMessage().removeObserver(onErrorMessage)
        viewModel.getOnReadyToShowTermsDialog().removeObserver(onReadyToShowTermsDialog)
        viewModel.getShowLoginScreen().removeObserver(showLoginScreenObserver)
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
