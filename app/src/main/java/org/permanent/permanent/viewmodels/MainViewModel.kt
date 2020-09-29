package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.ui.IS_WELCOME_SEEN

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentAccount = MutableLiveData<String>()
    private val currentSpaceUsed = MutableLiveData<Int>()

    fun getCurrentAccount(): MutableLiveData<String> {
        return currentAccount
    }

    fun getCurrentSpaceUsed(): MutableLiveData<Int> {
        return currentSpaceUsed
    }

    fun setWelcomeDialogSeen(preferences: SharedPreferences) {
        with(preferences.edit()) {
            putBoolean(IS_WELCOME_SEEN, true)
            apply()
        }
    }

    fun isWelcomeDialogSeen(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(IS_WELCOME_SEEN, false)
    }
}