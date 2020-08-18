package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences

class OnboardingViewModel(application: Application) : ObservableAndroidViewModel(application) {

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