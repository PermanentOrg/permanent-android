package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentAccount = MutableLiveData<String>()
    private val currentSpaceUsed = MutableLiveData<Int>()
    private val errorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onNavigateToLoginFragment = SingleLiveEvent<Void>()
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getCurrentAccount(): MutableLiveData<String> {
        return currentAccount
    }

    fun getCurrentSpaceUsed(): MutableLiveData<Int> {
        return currentSpaceUsed
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnNavigateToLoginFragment(): LiveData<Void> {
        return onNavigateToLoginFragment
    }

    fun logout() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        authRepository.logout(object : IAuthenticationRepository.IOnLogoutListener {
            override fun onSuccess() {
                isBusy.value = false
                onNavigateToLoginFragment.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                errorMessage.value = error
            }
        })
    }
}