package org.permanent.permanent.ui.archiveOnboarding

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityArchiveOnboardingBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.ui.computeWindowSizeClasses
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

class ArchiveOnboardingActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var viewModel: ArchiveOnboardingViewModel
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

        viewModel = ViewModelProvider(this)[ArchiveOnboardingViewModel::class.java]

        binding = DataBindingUtil.setContentView(this, R.layout.activity_archive_onboarding)
        binding.executePendingBindings()
        binding.lifecycleOwner = this
    }

    private val onArchiveOnboardingDone = Observer<Void?> {
        startActivity(Intent(this@ArchiveOnboardingActivity, MainActivity::class.java))
        finish()
    }

    override fun connectViewModelEvents() {
        viewModel.getOnArchiveOnboardingDone().observe(this, onArchiveOnboardingDone)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getOnArchiveOnboardingDone().removeObserver(onArchiveOnboardingDone)
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