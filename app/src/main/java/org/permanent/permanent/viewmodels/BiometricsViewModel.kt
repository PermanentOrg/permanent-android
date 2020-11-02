package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.infinum.goldfinger.Goldfinger
import co.infinum.goldfinger.MissingHardwareException
import co.infinum.goldfinger.NoEnrolledFingerprintException
import org.permanent.permanent.Constants
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class BiometricsViewModel(application: Application) : ObservableAndroidViewModel(application) {
    private val context = application.applicationContext
    private var prefsHelper = PreferencesHelper(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))
    private var goldFinger = Goldfinger.Builder(context).build()
    private val isBusy = MutableLiveData<Boolean>()
    private val onNavigateToMainActivity = SingleLiveEvent<Void>()
    private val onNavigateToLoginFragment = SingleLiveEvent<Void>()
    private val onShowOpenSettingsQuestionDialog = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    private val errorStringId = MutableLiveData<Int>()
    private var authRepository: IAuthenticationRepository = AuthenticationRepositoryImpl(application)
    private lateinit var promptParams: Goldfinger.PromptParams

    init {
        if(skipLogin()) {
            onNavigateToMainActivity.call()
        } else if (skipBiometrics()) onNavigateToLoginFragment.call()
    }

    private fun skipLogin(): Boolean {
        return  prefsHelper.isUserLoggedIn()
                && !goldFinger.canAuthenticate()
                && !goldFinger.hasFingerprintHardware()
    }

    private fun skipBiometrics(): Boolean {
        return !prefsHelper.isUserLoggedIn()
    }

    fun buildPromptParams(fragment: Fragment) {
        promptParams = Goldfinger.PromptParams.Builder(fragment)
            .title(R.string.login_biometric_title)
            .description(R.string.login_biometric_message)
            .deviceCredentialsAllowed(true)
            .negativeButtonText(R.string.cancel_button)
            .build()
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnNavigateToMainActivity(): LiveData<Void> {
        return onNavigateToMainActivity
    }

    fun getOnNavigateToLoginFragment(): LiveData<Void> {
        return onNavigateToLoginFragment
    }

    fun getOnShowOpenSettingsQuestionDialog(): LiveData<Void> {
        return onShowOpenSettingsQuestionDialog
    }

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    fun getErrorStringId(): LiveData<Int> {
        return errorStringId
    }

    fun authenticateUser() {
        goldFinger.authenticate(promptParams, object : Goldfinger.Callback {
            override fun onError(exception: Exception) {
                when (exception) {
                    is NoEnrolledFingerprintException -> handleResult(Goldfinger.Reason.NO_BIOMETRICS)
                    is MissingHardwareException -> handleResult(Goldfinger.Reason.HW_NOT_PRESENT)
                    else -> exception.message?.let { errorMessage.value = it }
                }
            }
            override fun onResult(result: Goldfinger.Result) {
                if (result.type() == Goldfinger.Type.SUCCESS)
                    handleResult(Goldfinger.Reason.AUTHENTICATION_SUCCESS)
                else if (result.type() != Goldfinger.Type.INFO)
                    handleResult(result.reason())
            }
        })
    }

    fun handleResult(reason: Goldfinger.Reason) {
        var messageId = 0
        when (reason) {
            Goldfinger.Reason.CANCELED -> messageId = 0
            Goldfinger.Reason.USER_CANCELED -> messageId = 0
            Goldfinger.Reason.AUTHENTICATION_START -> messageId = 0
            Goldfinger.Reason.AUTHENTICATION_SUCCESS -> onNavigateToMainActivity.call()
            Goldfinger.Reason.NO_BIOMETRICS -> onShowOpenSettingsQuestionDialog.call()
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
                onNavigateToLoginFragment.call()
            }

            override fun onFailed(error: String?) {
                isBusy.value = false
                when (error) {
                    Constants.ERROR_SERVER_ERROR,
                    Constants.ERROR_NO_API_KEY -> errorStringId.value = R.string.server_error
                    else -> errorMessage.value = error
                }
            }
        })
    }
}