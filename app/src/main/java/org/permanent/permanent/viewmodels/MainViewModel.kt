package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences
import org.permanent.permanent.Constants

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {

    fun setWelcomeDialogSeen(preferences: SharedPreferences) {
        with(preferences.edit()) {
            putBoolean(Constants.IS_WELCOME_SEEN, true)
            apply()
        }
    }

    fun isWelcomeDialogSeen(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(Constants.IS_WELCOME_SEEN, false)
    }
}