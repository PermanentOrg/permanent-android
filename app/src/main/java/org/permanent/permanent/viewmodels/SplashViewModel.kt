package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.ui.IS_ONBOARDING_COMPLETED

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedInResponse = SingleLiveEvent<Boolean>()
    private val authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun isOnboardingCompleted(preferences: SharedPreferences): Boolean {
        return preferences.getBoolean(IS_ONBOARDING_COMPLETED, false)
    }

    fun getOnLoggedInResponse(): MutableLiveData<Boolean> {
        return onLoggedInResponse
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun checkIsUserLoggedIn() {
        isBusy.value = true
        authRepository.verifyLoggedIn(object : IAuthenticationRepository.IOnLoggedInListener {
            override fun onResponse(isLoggedIn: Boolean) {
                isBusy.value = false
                onLoggedInResponse.value = isLoggedIn
            }
        })
    }
}