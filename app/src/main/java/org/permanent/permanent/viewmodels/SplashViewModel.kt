package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class SplashViewModel(application: Application) : ObservableAndroidViewModel(application) {

    private val onLoggedInResponse = SingleLiveEvent<Boolean>()
    private val authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getOnLoggedInResponse(): MutableLiveData<Boolean> {
        return onLoggedInResponse
    }

    fun verifyIsUserLoggedIn() {
        authRepository.verifyLoggedIn(object : IAuthenticationRepository.IOnLoggedInListener {
            override fun onResponse(isLoggedIn: Boolean) {
                onLoggedInResponse.value = isLoggedIn
            }
        })
    }
}