package org.permanent.permanent.ui.archiveOnboarding

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityArchiveOnboardingBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.computeWindowSizeClasses

class ArchiveOnboardingActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivityArchiveOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup orientation
        requestedOrientation = if (resources.getBoolean(R.bool.is_tablet)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, MODE_PRIVATE))
        val windowWidthSizeClass = computeWindowSizeClasses().windowWidthSizeClass
        prefsHelper.saveWindowWidthSizeClass(windowWidthSizeClass)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_archive_onboarding)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    //
//    private val onShowMessage = Observer<String> { message ->
//        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
//        val view: View = snackBar.view
//        view.setBackgroundColor(ContextCompat.getColor(this, R.color.deepGreen))
//        snackBar.setTextColor(ContextCompat.getColor(this, R.color.paleGreen))
//        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
//        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
//        snackBar.show()
//    }
//
//    private val onShowError = Observer<String> { message ->
//        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
//        val view: View = snackBar.view
//        view.setBackgroundColor(ContextCompat.getColor(this, R.color.deepRed))
//        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
//        snackBar.show()
//    }
//
//    private val onShowNextFragment = Observer<Fragment> {
//        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.frameLayoutContainer, it).commit()
//    }
//
//    private val onArchiveOnboardingDone = Observer<Void?> {
//        startActivity(Intent(this@ArchiveOnboardingActivity, MainActivity::class.java))
//        finish()
//    }
//
    override fun connectViewModelEvents() {
//        viewModel.getShowMessage().observe(this, onShowMessage)
//        viewModel.getShowError().observe(this, onShowError)
//        viewModel.getOnShowNextFragment().observe(this, onShowNextFragment)
//        viewModel.getOnArchiveOnboardingDone().observe(this, onArchiveOnboardingDone)
    }

    //
    override fun disconnectViewModelEvents() {
//        viewModel.getShowMessage().removeObserver(onShowMessage)
//        viewModel.getShowError().removeObserver(onShowError)
//        viewModel.getOnShowNextFragment().removeObserver(onShowNextFragment)
//        viewModel.getOnArchiveOnboardingDone().removeObserver(onArchiveOnboardingDone)
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