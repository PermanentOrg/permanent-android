package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData

class OnboardingViewModel(application: Application) : ObservableAndroidViewModel(application) {

    var snapPosition = MutableLiveData(0)

    fun setOnboardingCompleted(preferences: SharedPreferences) {
        with(preferences.edit()) {
            putBoolean(ONBOARDING_COMPLETED, true)
            apply()
        }
    }

    fun isOnboardingCompleted(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(ONBOARDING_COMPLETED, false)
    }

    companion object {
        const val ONBOARDING_COMPLETED = "onboarding_completed"
    }
}