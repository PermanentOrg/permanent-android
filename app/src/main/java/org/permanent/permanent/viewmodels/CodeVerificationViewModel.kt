package org.permanent.permanent.viewmodels

import android.app.Application
import android.content.Context
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.permanent.permanent.Constants
import org.permanent.permanent.PermanentApplication
import org.permanent.permanent.R
import org.permanent.permanent.repositories.AuthenticationRepositoryImpl
import org.permanent.permanent.repositories.IAuthenticationRepository
import org.permanent.permanent.ui.PREFS_NAME
import org.permanent.permanent.ui.PreferencesHelper

class CodeVerificationViewModel(application: Application) :
    ObservableAndroidViewModel(application) {

    private val appContext = application.applicationContext
    var isSmsCodeFlow = false
    private val currentCode = MutableLiveData<String>()
    private val codeError = MutableLiveData<Int>()
    private val isBusy = MutableLiveData<Boolean>()
    private val onCodeVerified = SingleLiveEvent<Void>()
    private val onLoggedIn = SingleLiveEvent<Void>()
    private val errorMessage = MutableLiveData<String>()
    private var authRepository: IAuthenticationRepository =
        AuthenticationRepositoryImpl(application)

    fun getVerificationCode(): MutableLiveData<String> {
        return currentCode
    }

    fun onCurrentCodeChanged(code: Editable) {
        currentCode.value = code.toString()
    }

    fun getCodeError(): LiveData<Int> {
        return codeError
    }

    fun getIsBusy(): MutableLiveData<Boolean> {
        return isBusy
    }

    fun getOnCodeVerified(): MutableLiveData<Void> = onCodeVerified

    fun getOnLoggedIn(): MutableLiveData<Void> = onLoggedIn

    fun getErrorMessage(): LiveData<String> {
        return errorMessage
    }

    private fun isCodeValid(): Boolean {
        currentCode.value = currentCode.value?.trim()
        val trimmedCode = currentCode.value

        if (trimmedCode.isNullOrEmpty()) {
            codeError.value = R.string.verification_code_empty_error
            return false
        } else {
            if (trimmedCode.length < Constants.VERIFICATION_CODE_LENGTH
                || trimmedCode.length > Constants.VERIFICATION_CODE_LENGTH
            ) {
                codeError.value = R.string.verification_code_length_error
                return false
            }
        }
        codeError.value = null
        return true
    }

    fun done() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        if (!isCodeValid()) return

        currentCode.value?.let {
            isBusy.value = true
            authRepository.verifyCode(it, getAuthType(),
                object : IAuthenticationRepository.IOnVerifyListener {
                    override fun onSuccess() {
                        isBusy.value = false
                        onCodeVerified.call()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        if (error.equals("warning.auth.token_does_not_match")) {
                            errorMessage.value =
                                PermanentApplication.instance.getString(R.string.verification_code_invalid_error)
                        } else {
                            errorMessage.value = error
                        }
                    }
                })
        }
    }

    private fun getAuthType(): String {
        if (isSmsCodeFlow) return Constants.AUTH_TYPE_PHONE
        return Constants.AUTH_TYPE_MFA_VALIDATION
    }

    fun tryLoginAgain() {
        if (isBusy.value != null && isBusy.value!!) {
            return
        }
        val prefsHelper = PreferencesHelper(
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        )
        val userEmail = prefsHelper.getUserEmail()
        val userPass = prefsHelper.getUserPass()

        if (userEmail != null && userPass != null) {
            isBusy.value = true
            authRepository.login(
                userEmail,
                userPass,
                object : IAuthenticationRepository.IOnLoginListener {
                    override fun onSuccess() {
                        isBusy.value = false
                        onLoggedIn.call()
                    }

                    override fun onFailed(error: String?) {
                        isBusy.value = false
                        errorMessage.value = error
                    }
                })
        }
    }
}