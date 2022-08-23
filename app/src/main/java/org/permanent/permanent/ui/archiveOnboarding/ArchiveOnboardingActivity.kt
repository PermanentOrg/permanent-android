package org.permanent.permanent.ui.archiveOnboarding

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityArchiveOnboardingBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.activities.MainActivity
import org.permanent.permanent.ui.activities.PermanentBaseActivity
import org.permanent.permanent.viewmodels.ArchiveOnboardingViewModel

class ArchiveOnboardingActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivityArchiveOnboardingBinding
    private lateinit var viewModel: ArchiveOnboardingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_archive_onboarding)
        viewModel = ViewModelProvider(this)[ArchiveOnboardingViewModel::class.java]
        binding.executePendingBindings()
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        prefsHelper = PreferencesHelper(getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
        setSupportActionBar(binding.toolbar)
    }

    private fun showFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayoutContainer, fragment).commit()
    }

    private val onShowMessage = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.paleGreen))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.green))
        val snackbarTextTextView = view.findViewById(R.id.snackbar_text) as TextView
        snackbarTextTextView.setTypeface(snackbarTextTextView.typeface, Typeface.BOLD)
        snackBar.show()
    }

    private val onShowError = Observer<String?> { message ->
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
        val view: View = snackBar.view
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.deepRed))
        snackBar.setTextColor(ContextCompat.getColor(this, R.color.white))
        snackBar.show()
    }

    private val onShowNextFragment = Observer<Fragment> {
        showFragment(it)
    }

    private val onArchiveOnboardingDone = Observer<Void> {
        startActivity(Intent(this@ArchiveOnboardingActivity, MainActivity::class.java))
        finish()
    }

    override fun connectViewModelEvents() {
        viewModel.getShowMessage().observe(this, onShowMessage)
        viewModel.getShowError().observe(this, onShowError)
        viewModel.getOnShowNextFragment().observe(this, onShowNextFragment)
        viewModel.getOnArchiveOnboardingDone().observe(this, onArchiveOnboardingDone)
    }

    override fun disconnectViewModelEvents() {
        viewModel.getShowMessage().removeObserver(onShowMessage)
        viewModel.getShowError().removeObserver(onShowError)
        viewModel.getOnShowNextFragment().removeObserver(onShowNextFragment)
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