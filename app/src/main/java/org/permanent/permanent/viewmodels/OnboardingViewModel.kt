package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants

class OnboardingViewModel(application: Application) : ObservableAndroidViewModel(application) {

    var snapPosition = MutableLiveData(0)

    fun setOnboardingCompleted(preferences: SharedPreferences) {
        with(preferences.edit()) {
            putBoolean(Constants.IS_ONBOARDING_COMPLETED, true)
            apply()
        }
    }
}