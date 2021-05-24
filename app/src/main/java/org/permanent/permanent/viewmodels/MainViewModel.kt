package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.BuildConfig
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class MainViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val currentAccount = MutableLiveData<String>()
    private val currentSpaceUsed = MutableLiveData<Int>()
    private val errorMessage = MutableLiveData<String>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onLoggedOut = SingleLiveEvent<Void>()
    val versionName = MutableLiveData(application.getString(
        R.string.version_text, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE.toString()))
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

    fun getOnLoggedOut(): LiveData<Void> {
        return onLoggedOut
    }

    fun logout() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }

        isBusy.value = true
        authRepository.logout(object : IAuthenticationRepository.IOnLogoutListener {
            override fun onSuccess() {
                isBusy.value = false
                onLoggedOut.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                errorMessage.value = error
            }
        })
    }
}