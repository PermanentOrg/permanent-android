package org.permanent.permanent.ui.activities

import androidx.appcompat.app.AppCompatActivity


abstract class PermanentBaseActivity : AppCompatActivity() {

    abstract fun connectViewModelEvents()

    abstract fun disconnectViewModelEvents()
}