package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.repositories.ILoginRepository
import org.permanent.permanent.repositories.LoginRepositoryImpl

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedInResponse = SingleLiveEvent<Boolean>()
    private val loginRepository: ILoginRepository = LoginRepositoryImpl(application)

    fun isOnboardingCompleted(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(Constants.IS_ONBOARDING_COMPLETED, false)
    }

    fun getOnLoggedInResponse(): MutableLiveData<Boolean> {
        return onLoggedInResponse
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun checkIsUserLoggedIn() {
        isBusy.value = true
        loginRepository.checkIsUserLoggedIn(object : ILoginRepository.IOnLoggedInListener {
            override fun onResponse(isLoggedIn: Boolean) {
                isBusy.value = false
                onLoggedInResponse.value = isLoggedIn
            }
        })
    }
}