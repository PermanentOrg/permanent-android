package org.permanent.permanent.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import org.permanent.permanent.R
import org.permanent.permanent.databinding.ActivityUpdateAppBinding
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper
import org.permanent.permanent.ui.computeWindowSizeClasses

class UpdateAppActivity : PermanentBaseActivity() {

    private lateinit var prefsHelper: PreferencesHelper
    private lateinit var binding: ActivityUpdateAppBinding

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

        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_app)
        binding.btnUpdate.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
//                        Uri.parse("market://details?id=org.permanent.PermanentArchive")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }
    }

    override fun connectViewModelEvents() {
    }

    override fun disconnectViewModelEvents() {
    }
}