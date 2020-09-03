package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {

    fun setWelcomeDialogSeen(preferences: SharedPreferences) {
        with(preferences.edit()) {
            putBoolean(IS_WELCOME_SEEN, true)
            apply()
        }
    }

    fun isWelcomeDialogSeen(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(IS_WELCOME_SEEN, false)
    }

    companion object {
        const val IS_WELCOME_SEEN = "is_welcome_seen"
    }
}