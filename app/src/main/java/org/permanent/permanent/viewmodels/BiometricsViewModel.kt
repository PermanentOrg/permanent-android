package org.permanent.permanent.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.infinum.goldfinger.Goldfinger
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository

class BiometricsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val isBusy = MutableLiveData<Boolean>()
    private val onBiometricAuthSuccess = SingleLiveEvent<Void>()
    private val onBiometricsUnregistered = SingleLiveEvent<Void>()
    private val onLoggedOut = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    private val errorStringId = MutableLiveData<Int>()

    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnBiometricAuthSuccess(): LiveData<Void> {
        return onBiometricAuthSuccess
    }

    fun getOnBiometricsUnregistered(): LiveData<Void> {
        return onBiometricsUnregistered
    }

    fun getOnLoggedOut(): MutableLiveData<Void> {
        return onLoggedOut
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun getErrorStringId(): LiveData<Int> {
        return errorStringId
    }

    fun handleResult(reason: Goldfinger.Reason) {
        var messageId = 0
        when (reason) {
            Goldfinger.Reason.CANCELED -> messageId = 0
            Goldfinger.Reason.USER_CANCELED -> messageId = 0
            Goldfinger.Reason.AUTHENTICATION_START -> messageId = 0
            Goldfinger.Reason.AUTHENTICATION_SUCCESS -> onBiometricAuthSuccess.call()
            Goldfinger.Reason.NO_BIOMETRICS -> onBiometricsUnregistered.call()
            Goldfinger.Reason.HW_NOT_PRESENT ->
                messageId = R.string.login_biometric_error_no_biometric_hardware
            Goldfinger.Reason.HARDWARE_UNAVAILABLE ->
                messageId = R.string.login_biometric_error_unavailable
            Goldfinger.Reason.TIMEOUT ->
                messageId = R.string.login_biometric_error_timeout
            Goldfinger.Reason.LOCKOUT,
            Goldfinger.Reason.LOCKOUT_PERMANENT -> {
                messageId = R.string.login_biometric_error_too_many_failed_attempts
                logout()
            }
            Goldfinger.Reason.NO_DEVICE_CREDENTIAL,
            Goldfinger.Reason.NEGATIVE_BUTTON,
            Goldfinger.Reason.UNABLE_TO_PROCESS,
            Goldfinger.Reason.VENDOR,
            Goldfinger.Reason.NO_SPACE,
            Goldfinger.Reason.AUTHENTICATION_FAIL,
            Goldfinger.Reason.UNKNOWN -> messageId = R.string.login_biometric_error_failed
        }
        if (messageId != 0) errorStringId.value = messageId
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