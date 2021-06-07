package org.permanent.permanent.ui.activities

import androidx.appcompat.app.AppCompatActivity
import org.permanent.permanent.PermanentApplication


abstract class PermanentBaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        PermanentApplication.instance.currentActivity = this
    }

    abstract fun connectViewModelEvents()

    abstract fun disconnectViewModelEvents()
}